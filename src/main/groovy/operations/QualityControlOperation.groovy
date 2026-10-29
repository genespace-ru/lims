package operations;

import javax.inject.Inject

import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.beans.DynamicPropertySetSupport
import java.nio.file.Path
import java.util.Map.Entry

import ru.biosoft.access.core.DataElementPath
import ru.biosoft.access.file.GenericFileDataCollection
import ru.biosoft.access.DataCollectionUtils
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.util.TempFiles
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.lims.SystemSettings
import ru.biosoft.nextflow.NextflowService
import biouml.plugins.wdl.GeneSpaceContext
import biouml.plugins.wdl.WorkflowSettings
import biouml.plugins.wdl.nextflow.NextFlowRunner

import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.be5.databasemodel.util.DpsUtils

/**
 * Runs FastQC + MultiQC quality control on project FASTQ files.
 *
 * Attached to both the workflow_runs "ForProjectCard" query (Workflows tab)
 * and the samples "ForProjectCard" query (Samples tab).
 *
 * - Workflows tab (no rows selected): collects all *.fastq.gz, *.fq.gz from the
 *   project's samples folder.
 * - Samples tab (rows selected): collects *.fastq.gz, *.fq.gz files registered in
 *   file_info for the selected sample IDs only. If no ID selected, all samples of the project are taken.
 *
 * Results are published under the project's results folder; the Tower
 * complete callback triggers processWorkflowResults which registers output
 * files and parses the MultiQC FastQC summary into qc_runs.
 */
public class QualityControlOperation extends GOperationSupport {

    @Inject
    private RepositoryManager repo;
    @Inject
    protected NextflowService nf;

    Map<String, Object> presets

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        DPS params = new DynamicPropertySetSupport()
        presets = presetValues
        return null
    }

    @Override
    public void invoke(Object parameters) throws Exception {
        def workflowName = SystemSettings.getValueWithDefault("lims", "fastqc_workflow", "fastqc.nf")
        def qPars = new groovy.json.JsonSlurper().parseText( presets._params_ )
        String repoPath = repo.getRepositoryPath();
        if( repoPath == null )
            return
        def prjId = Long.parseLong( qPars["___prjID"].toString() )
        def prj = database.getEntity( "projects" ).get( prjId )
        if( prj == null )
            return

        DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(prj.$name );
        if( !projectPath.exists() )
            return
        GenericFileDataCollection prjDc = projectPath.getDataCollection();

        // Collect input FASTQ files based on invocation context.
        List<String> fastqFiles = collectFastqFiles( prjId, projectPath )
        if( fastqFiles.isEmpty() ) {
            setResult( OperationResult.error( "No FASTQ files found for quality control." ) )
            return
        }

        DataElementPath results = projectPath.getChildPath("results" )
        if( !results.exists() ) {
            DataCollectionUtils.createSubCollection(results);
        }
        File resultsDir = prjDc.getFile("results");

        GenericFileDataCollection workflowsDc = repo.getWorkflowsCollection();
        def workflowQCPath = workflowsDc.getCompletePath().getChildPath(workflowName )
        Long fileInfo = db.oneLong("SELECT id FROM file_info WHERE path = ?",  workflowQCPath.toString())
        if( fileInfo == null ) {
            setResult(OperationResult.error("Unknown file " + workflowQCPath.toString()))
            return
        }
        Long workflowInfoId = db.oneLong( "SELECT id FROM workflow_info WHERE file_info = ?", fileInfo)
        if( workflowInfoId == null ) {
            workflowInfoId = database.workflow_info << [file_info: fileInfo, title: "Quality control", description: "Контроль качества fastq ридов."]
        }

        String nextFlowScript = ApplicationUtils.readAsString(workflowsDc.getFile(workflowName ));
        Path outputDir = TempFiles.getTempDirectory().toPath()

        int workflowRunId = nf.insertWorkflowRun ((int)prj.$id, (int)workflowInfoId)
        db.update("UPDATE workflow_runs SET comment = ? WHERE id = ?", "workflow_run_"+workflowRunId, workflowRunId)

        String serverUrl = request.getServerUrl()
        String referer = null
        if( request != null && request.getRawRequest() != null ) {
            referer = request.getRawRequest().getHeader( "referer" )
        }
        if( referer != null ) {
            serverUrl = referer.replaceAll('/$', '')
        }
        def serverDockerUrl = SystemSettings.getValueWithDefault( "lims", "docker_server_url", null );
        if( serverDockerUrl != null ) {
            serverUrl = serverDockerUrl
        }
        def towerAddress = serverUrl+"/nf"

        boolean useDocker = false
        String useDockerStr = SystemSettings.getValueWithDefault( "lims", "docker_nextflow", null )
        if( useDockerStr != null ) {
            useDocker = Boolean.parseBoolean(useDockerStr )
        }

        Map<String, Object> nextflowParams = new LinkedHashMap<>();
        nextflowParams.put("reads", fastqFiles)
        nextflowParams.put("resultsDir", resultsDir.getAbsolutePath())

        WorkflowSettings settings = new WorkflowSettings();
        settings.setUseDocker(useDocker )
        settings.getNextflowSettings().setPublishOutput("copy")
        String stageInputMode = SystemSettings.getValueWithDefault( "lims", "stage_input_mode", "symlink" )
        if( stageInputMode != null && !stageInputMode.isBlank() ) {
            settings.getNextflowSettings().setStageInput( stageInputMode )
        }
        //TODO: fix in WorkflowSettings
        // Serialize params manually so List values become JSON arrays
        // (WorkflowSettings.generateParametersJSON stringifies lists).
        String json = groovy.json.JsonOutput.toJson( nextflowParams )
        File jsonFile = outputDir.resolve("parameters.json").toFile()
        ApplicationUtils.writeString(jsonFile, json)
        GeneSpaceContext nfContext = new GeneSpaceContext(repo.getProjectsPath(), repo.getWorkflowsPath(), repo.getGenomePath(), outputDir)
        NextFlowRunner.runNextFlow("${workflowRunId}", "fastqc", nextFlowScript, false, settings, towerAddress, nfContext, jsonFile.toString(), true)
        setResult(OperationResult.finished("Quality control started"))
    }

    /**
     * Collect FASTQ file paths for quality control.
     *
     * When rows are selected (context.records non-empty — Samples tab),
     * collect *.fastq.gz files from file_info for those sample IDs only.
     * When no rows are selected (Workflows tab), collect all *.fastq.gz
     * from the project's samples folder on disk.
     */
    private List<String> collectFastqFiles( long prjId, DataElementPath projectPath ) {
        List<String> files = new ArrayList<>()

        List<Long> fileTypes = getFastqTypes()
        if(fileTypes.isEmpty())
            return files
        String fileTypesPlaceholder = fileTypes.collect { "?" }.join(", ")
        // Check if specific samples were selected (Samples tab).
        // When on the Samples tab with no selection, use all project samples.
        boolean isSamplesTab = "samples".equals( info.getEntityName() )
        if( isSamplesTab ) {
            List<Long> sampleIds = new ArrayList<>()
            if( context.records != null ) {
                for( Object rec : context.records ) {
                    sampleIds << Long.parseLong( rec.toString() )
                }
            }

            def rows
            if( sampleIds.isEmpty() ) {
                // No samples selected — use all samples of this project
                rows = db.listWithParams( "SELECT fi.path FROM file_info fi " +
                        "WHERE fi.entity='samples' AND fi.fileType IN (${fileTypesPlaceholder}) AND fi.project = ?",
                        *(fileTypes + [prjId] as List) )
            } else {
                String placeholders = sampleIds.collect { "?" }.join(", ")
                rows = db.listWithParams( "SELECT fi.path FROM file_info fi " +
                        "WHERE fi.entity='samples' AND fi.entityID IN (${placeholders}) " +
                        " AND fi.fileType IN (${fileTypesPlaceholder}) AND fi.project = ?",
                        *(sampleIds + fileTypes + [prjId] as List) )
            }
            for( def row : rows ) {
                String filePathStr = row.$path
                if( filePathStr == null || filePathStr.isBlank() )
                    continue
                DataElementPath filePath = DataElementPath.create(filePathStr)
                if( filePath.exists() ) {
                    File f = filePath.getParentPath().getDataElement(GenericFileDataCollection.class).getFile(filePath.getName())
                    if( f != null )
                        files << f.getAbsolutePath()
                }
            }
        } else {
            // Workflows tab: collect all FASTQ files registered in file_info
            // under the project's samples folder
            def prjName = db.oneString("SELECT name FROM projects WHERE id=?", prjId )
            def rows = db.listWithParams( "SELECT fi.path FROM file_info fi " +
                    "WHERE fi.fileType IN (${fileTypesPlaceholder}) AND fi.project = ? " +
                    "AND fi.path LIKE ? ", *(fileTypes + [
                        prjId,
                        "projects/" + prjName + "/samples/%"
                    ] as List) )

            for( def row : rows ) {
                String filePathStr = row.$path
                if( filePathStr == null || filePathStr.isBlank() )
                    continue
                DataElementPath filePath = DataElementPath.create(filePathStr)
                if( filePath.exists() ) {
                    File f = filePath.getParentPath().getDataElement(GenericFileDataCollection.class).getFile(filePath.getName())
                    if( f != null )
                        files << f.getAbsolutePath()
                }
            }
        }

        return files
    }

    public List<Long> getFastqTypes() {
        return db.scalarLongList("SELECT id FROM file_types WHERE suffix LIKE '%.fastq.gz' OR suffix LIKE '%.fastq' OR suffix LIKE '%.fq.gz' OR suffix LIKE '%.fq'" )
    }

    public static DynamicPropertySet initParameters(Map<String, Object> params) {
        DynamicPropertySet parameters = new DynamicPropertySetSupport();
        for ( Entry<String, Object> param : params.entrySet() ) {
            String name = param.getKey();
            Object value = param.getValue();
            Class clazz = String.class;
            DynamicProperty dp = new DynamicProperty( name, clazz, value );
            parameters.add( dp );
        }
        return parameters;
    }
}
