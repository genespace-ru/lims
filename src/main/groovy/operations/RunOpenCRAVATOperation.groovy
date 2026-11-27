package operations;

import java.nio.file.Path
import java.util.Map.Entry

import java.util.logging.Logger

import javax.inject.Inject

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import biouml.model.Diagram
import biouml.plugins.wdl.GeneSpaceContext
import biouml.plugins.wdl.diagram.WDLImporter
import biouml.plugins.wdl.nextflow.NextFlowGenerator
import biouml.plugins.wdl.nextflow.NextFlowRunner
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ru.biosoft.access.core.DataElementPath
import ru.biosoft.access.DataCollectionUtils
import ru.biosoft.access.file.GenericFileDataCollection
import ru.biosoft.lims.loader.VCFLoader
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.lims.SystemSettings
import ru.biosoft.nextflow.NextflowService
import ru.biosoft.opencravat.OpenCravatLauncher
import ru.biosoft.opencravat.ProcessProgress
import ru.biosoft.lims.util.FileTypeUtils
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.util.RepositoryFileUtils
import ru.biosoft.util.TempFiles

import biouml.plugins.wdl.WorkflowSettings

public class RunOpenCRAVATOperation extends GOperationSupport {

    private static final Logger log = Logger.getLogger( RunOpenCRAVATOperation.class.getName() )

    @Inject
    private RepositoryManager repo;
    @Inject
    protected NextflowService nf;
    Map<String, Object> presets
    private OpenCravatLauncher launcher = null;

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        presets = presetValues
        DynamicPropertySetSupport params = new DynamicPropertySetSupport()


        String prjIdStr = (String) presetValues.get("___prjID")
        if (prjIdStr == null || prjIdStr.isBlank()) {
            prjIdStr = (String) context.params.get("___prjID")
        }
        if (prjIdStr == null || prjIdStr.isBlank()) {
            setResult(OperationResult.error("Project ID is not available."))
            return null
        }
        long prjId = Long.parseLong(prjIdStr)
        params._prjID_ = [value: prjIdStr, DISPLAY_NAME: "Project ID", TYPE: String, HIDDEN: true]
        def prjRow = db.recordWithParams("SELECT name FROM projects WHERE id = ?", prjId)
        String projectName = prjRow?.getString("name")

        // Collect project VCF files (sample-assigned only).
        // Tag value = "sampleId:absPath" (the sample names the snv_ tables); label = relative path.
        Map<String, VcfFileEntry> projectFiles = getProjectVCFFiles(prjId)
        String[][] fileTags = projectFiles.collect { rel, e -> [
                e.sampleId + ":" + e.file.getAbsolutePath(),
                rel
            ] as String[] } as String[][]
        DynamicProperty inputVCFProp = new DynamicProperty("inputVCF", "Input VCF", String.class)
        inputVCFProp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, fileTags)
        inputVCFProp.setAttribute(BeanInfoConstants.READ_ONLY, false)
        inputVCFProp.setAttribute(BeanInfoConstants.CAN_BE_NULL, false)
        params.add(inputVCFProp)

        initLauncher()
        def annotators = launcher.getAnnotators()
        String[][] annotatorTags = annotators.collect { name, title -> [name, title] as String[] } as String[][]
        DynamicProperty annotatorsProp = new DynamicProperty("annotators", "Annotators", String.class)
        annotatorsProp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, annotatorTags)
        annotatorsProp.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true)
        annotatorsProp.setAttribute(BeanInfoConstants.CAN_BE_NULL, true)
        params.add(annotatorsProp)

        def reporters = launcher.getReporters()
        String[][] reportersTags = reporters.collect { name, title -> [name, title] as String[] } as String[][]
        DynamicProperty reportersProp = new DynamicProperty("reporter", "Report type", String.class)
        reportersProp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, reportersTags)
        reportersProp.setAttribute(BeanInfoConstants.CAN_BE_NULL, true)
        params.add(reportersProp)

        params = DpsUtils.setValues(params, presetValues)

        return params
    }

    /**
     * Whether the VCF uses UCSC-style "chr"-prefixed contigs (e.g. chr1, chrM).
     * Such files need a chromosome-name mapping for the genome browser to align
     * the track to a reference assembly built from Ensembl-style names.
     *
     * <p>Detects this from the VCF header's {@code ##contig=<ID=chr1,...>} lines
     * and, if there are none, from the CHROM column of the first data row. Only
     * scans the header / first record, so it is cheap even for large VCFs.
     */
    private boolean hasChrPrefixedContigs( File vcfFile ) {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(vcfFile))) {
            String line
            while ((line = br.readLine()) != null) {
                if (line.startsWith("##contig=")) {
                    // ##contig=<ID=chr1,length=...,...>  -> the ID is the contig name
                    int idIdx = line.indexOf("ID=")
                    if (idIdx >= 0) {
                        int start = idIdx + 3
                        int end = line.indexOf(',', start)
                        String contig = (end >= 0 ? line.substring(start, end) : line.substring(start))
                        if (contig.startsWith("chr"))
                            return true
                    }
                }
                else if (line.startsWith("#"))
                    continue  // other header line
                else {
                    // first data row: CHROM is the first tab-separated field
                    int tab = line.indexOf('\t')
                    String chrom = tab >= 0 ? line.substring(0, tab) : line
                    return chrom.startsWith("chr")
                }
            }
        }
        catch (java.io.IOException e) {
            log.warning("Could not inspect " + vcfFile.getName() + " for chr-prefixed contigs: " + e.getMessage())
        }
        return false
    }

    private void importVCFtoSql( File vcfFile, String projectName, Long sampleId ) throws Exception {
        // VCFLoader.load() drops and recreates snv_<sample> / snv_meta_<sample>.
        // Name the tables after the sample the input VCF belongs to, so the
        // SNV viewer (samples.yaml) can find them via samples.ID.
        def loader = new VCFLoader()
        loader.setDb( db )
        loader.load( vcfFile, projectName, sampleId.toString() )
        log.info( "OpenCRAVAT output VCF " + vcfFile.getName() + " imported into snv_" + sampleId )
    }

    private synchronized void initLauncher() {
        if(launcher == null) {
            String openCravatDir = SystemSettings.getValueWithDefault( "lims", "opencravat_dir", null )
            launcher =  OpenCravatLauncher.getInstance(openCravatDir );
        }
    }

    /**
     * A VCF candidate file: its absolute path plus the sample it is assigned to
     * (via file_info.entity='samples'). The sample names the resulting
     * snv_&lt;sample&gt; tables.
     */
    private static class VcfFileEntry {
        File file
        long sampleId
    }

    /**
     * Collect sample-assigned VCF files for the given project from file_info.
     * Only files registered with entity='samples' and a sample ID are returned —
     * the sample is what the resulting snv_&lt;sample&gt; tables are named after,
     * and the SNV viewer in samples.yaml looks them up by samples.ID.
     *
     * @return Map: display label (path relative to the project folder) -> VcfFileEntry
     */
    private Map<String, VcfFileEntry> getProjectVCFFiles(long prjId) throws Exception {
        Map<String, VcfFileEntry> result = new LinkedHashMap<>()
        def prjRow = db.recordWithParams("SELECT name FROM projects WHERE id = ?", prjId)
        String projectName = prjRow?.getString("name")
        if (projectName == null) return result

        String repoPath = repo.getRepositoryPath()
        if (repoPath == null) return result

        DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(projectName )
        if (projectPath.exists()) {
            def rows = db.list("SELECT path, entityID FROM file_info fi "+
                    "INNER JOIN file_types ft ON ft.id = fi.filetype " +
                    "WHERE project = ${prjId} AND ft.suffix='.vcf' "+
                    "AND fi.entity = 'samples' AND fi.entityID IS NOT NULL ORDER BY fi.ID,path")
            for (def row : rows) {
                String filePathStr = row.$path
                if (filePathStr == null || filePathStr.isBlank())
                    continue
                def sampleIdVal = row.$entityID
                if (sampleIdVal == null)
                    continue

                DataElementPath filePath = DataElementPath.create(filePathStr)
                // filePathStr is a complete repo-root-relative path (e.g. "projects/<prj>/...").
                if (filePath.exists()) {
                    // Resolve the absolute file via the parent collection (single walk).
                    File f = filePath.getParentPath().getDataElement(GenericFileDataCollection.class).getFile(filePath.getName())
                    if (f != null) {
                        VcfFileEntry entry = new VcfFileEntry()
                        entry.file = f
                        entry.sampleId = Long.parseLong(sampleIdVal.toString())
                        result.put(filePath.getPathDifference(projectPath ), entry)
                    }
                }
            }
        }
        return result
    }


    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        Long projectId = params.getValue("_prjID_") == null ? null : Long.parseLong( params.getValue("_prjID_").toString() )
        if (projectId == null) {
            setResult(OperationResult.error("Project ID is not available."))
            return
        }

        // Input VCF (a file dropdown value — "sampleId:absPath")
        def inputVcfValue = params.getValue("inputVCF")
        String inputVcfRaw = inputVcfValue == null ? "" : inputVcfValue.toString()
        if (inputVcfRaw.isBlank()) {
            setResult(OperationResult.error("Please select an input VCF file."))
            return
        }
        int sepIdx = inputVcfRaw.lastIndexOf(':')
        String inputVcfPath = (sepIdx >= 0) ? inputVcfRaw.substring(sepIdx + 1) : inputVcfRaw
        // The sample the input VCF is assigned to; the resulting snv_ tables are named after it.
        Long sampleId = (sepIdx >= 0) ? Long.parseLong(inputVcfRaw.substring(0, sepIdx)) : null
        File inputVCF = new File( inputVcfPath )
        if (!inputVCF.exists() ) {
            setResult(OperationResult.error("Input VCF file not found: " + inputVCF))
            return
        }

        // Annotators (multi-select; nullable)
        def annotatorsValue = params.getValue("annotators")
        List<String> annotators = new ArrayList<>()
        if (annotatorsValue != null) {
            List<?> annotatorsList = annotatorsValue instanceof Object[] ? (annotatorsValue as Object[]) as List : [annotatorsValue]
            for (def a : annotatorsList) {
                if (a != null && !a.toString().isBlank() )
                    annotators << a.toString()
            }
        }

        def reporterValue = params.getValue("reporter")
        String reportType = reporterValue == null ? "" : reporterValue.toString()

        String repoPath = repo.getRepositoryPath()

        def prj = database.getEntity( "projects" ).get( projectId )
        DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(prj.$name )

        DataElementPath results = projectPath.getChildPath("results" )
        GenericFileDataCollection resultsDc = DataCollectionUtils.createSubCollection(results)

        //initLauncher()

        int runId
        DataElementPath resultsPath
        File resultsDir
        try {
            runId = (int) (long) db.insert( "INSERT INTO workflow_runs (project, workflow_info, status) VALUES(?, ?, 'running')",
                    projectId, null )

            String runResultsSubdir = "workflow_run_${runId}"

            resultsPath = results.getChildPath(runResultsSubdir)
            if(!resultsPath.exists()) {
                DataCollectionUtils.createSubCollection(resultsPath);
            }

            resultsDir = resultsDc.getFile( runResultsSubdir )

            db.update( "UPDATE workflow_runs SET params = ?::json, comment = ? WHERE id = ?",
                    JsonOutput.toJson([inputVCF: inputVCF.getAbsolutePath(), annotators: annotators, sampleId: sampleId, reportType: reportType]),
                    runResultsSubdir, runId )
        }
        catch ( Exception e ) {
            log.severe("Failed to create workflow_runs row: " + e.getMessage())
            setResult(OperationResult.error("Failed to register the run: ${e.getMessage()}"))
            return
        }

        Thread t = new Thread( {
            ProcessProgress progress = new ProcessProgress() {
                        public void stdout(String msg) {
                            log.info("[opencravat " + runId + "] " + msg)
                        }
                        public void stderr(String msg) {
                            log.severe("[opencravat " + runId + "] " + msg)
                        }
                        public void finished(int exitCode) {
                            log.info("[opencravat " + runId + "] finished with exit code " + exitCode)
                        }
                    }
            try {
                List<File> resultFiles = launcher.runOpencravat( inputVCF, annotators, reportType, resultsDir, progress )
                //process result
                if (resultFiles.isEmpty()) {
                    throw new Exception( "OpenCRAVAT produced no output files of type '." + reportType + "'" )
                }
                // Import a VCF result to the sample's snv_ tables.
                if (reportType.equalsIgnoreCase("vcf") && sampleId != null) {
                    File vcfFile = resultFiles.find { it.name.endsWith(".vcf") }
                    if (vcfFile != null)
                        importVCFtoSql( vcfFile, prj.$name, sampleId )
                }
                // For VCF results that use UCSC-style "chr"-prefixed contigs (e.g. chr1),
                // record a chromosome-name mapping in the parent collection's .info so the
                // genome browser can align the track to the reference genome.
                if (reportType.equalsIgnoreCase("vcf")) {
                    GenericFileDataCollection runResultsDc = DataCollectionUtils.createSubCollection(resultsPath)
                    for (File f : resultFiles.findAll { it.name.endsWith(".vcf") }) {
                        if (hasChrPrefixedContigs(f)) {
                            runResultsDc.setFileInfo([
                                name: f.getName(),
                                type: "VCF track",
                                properties: [ ChrMapping: "databases/ChrMapping/ChrMapping/hg38_ucsc_to_ensembl.txt" ]
                            ] as Map<String,Object>)
                            log.info( "OpenCRAVAT run " + runId + ": set ChrMapping for " + f.getName() )
                        }
                    }
                }
                // Register every result file in file_info. The imported VCF (the
                // one whose rows are in the snv_<sample> table) is attached to the
                // SAMPLE, not the run, so the SNV viewer / genome-browser links can
                // resolve it via file_info.entity='samples'. Other result files
                // (gene/sample/mapping tables, logs) stay attached to the run.
                for (File f : resultFiles) {
                    DataElementPath fPath = resultsPath.getChildPath( f.getName() )
                    boolean isImportedVcf = f.name.endsWith(".vcf") && sampleId != null
                    String entity = isImportedVcf ? "samples" : "workflow_runs"
                    Long entityID = isImportedVcf ? sampleId : runId
                    db.insert( "INSERT INTO file_info (fileName, fileType, path, size, project, entity, entityID ) VALUES(?,?,?,?,?)",
                            f.getName(),
                            FileTypeUtils.getOrCreateTypeId( db, f ),
                            fPath.toString(),
                            f.length(),
                            projectId, entity, entityID)
                }
                db.update( "UPDATE workflow_runs SET status = ?, completion_time = NOW() WHERE id = ?", "finished", runId )
            }
            catch ( Exception e ) {
                log.severe( "OpenCRAVAT run " + runId + " failed: " + e.getMessage() )
                try {
                    db.update( "UPDATE workflow_runs SET status = ?, log = ?, completion_time = NOW() WHERE id = ?",
                            "failed", e.getMessage(), runId )
                }
                catch ( Exception dbEx ) {
                    log.severe( "OpenCRAVAT run " + runId + ": also failed to record failure status: " + dbEx.getMessage() )
                }
            }
        }, "opencravat-" + runId )
        t.setDaemon( true )
        t.start()

        setResult(OperationResult.finished("OpenCRAVAT started"))
    }
}
