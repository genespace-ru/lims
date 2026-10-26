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
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.nextflow.NextflowService
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.util.RepositoryFileUtils
import ru.biosoft.util.TempFiles

import biouml.plugins.wdl.WorkflowSettings

public class RunWorkflowOperation extends GOperationSupport {

    private static final Logger log = Logger.getLogger( RunWorkflowOperation.class.getName() )

    @Inject
    private RepositoryManager repo;
    @Inject
    protected NextflowService nf;
    Map<String, Object> presets

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

        //Load all workflows into a dropdown ----------------------
        def workflowTags = queries.getTagsFromCustomSelectionView(
                "workflow_info", "Workflow Selection")

        if (workflowTags == null || workflowTags.length == 0) {
            setResult(OperationResult.error("No workflows available."))
            return null
        }

        Object workflowID = context.getParams().getOrDefault("workflowID", presetValues.get("workflowID"))
        params.workflowID =[
            TYPE: Long,
            TAG_LIST_ATTR: workflowTags as String[][],
            RELOAD_ON_CHANGE: true,
            value: workflowID,
            DISPLAY_NAME: "Workflow"
        ]

        //Build parameter form by workflow params
        Long selectedWorkflowId = params.getValue("workflowID") as Long
        if (selectedWorkflowId != null) {
            params._workflowInfoId_ = [value: String.valueOf(selectedWorkflowId),
                DISPLAY_NAME: "Workflow ID", TYPE: String, HIDDEN: true]

            // Parse the params JSON from workflow_info.params
            def wfRow = db.recordWithParams(
                    "SELECT wi.params FROM workflow_info wi " +
                    "WHERE wi.id = ?",
                    selectedWorkflowId)

            if (wfRow != null) {
                String paramsJson = wfRow.getString("params")
                if (paramsJson != null && !paramsJson.isBlank()) {
                    def paramSpecs = new JsonSlurper().parseText(paramsJson)

                    // Collect files once — reused for all File-type params:
                    // project files and workflow files.
                    // Maps: display label relative to project/prjName or relative to databases -> absolute path,
                    Map<String, String> projectFiles = getProjectFiles(prjId)
                    Map<String, String> workflowFiles = getWorkflowFiles(selectedWorkflowId)

                    for (def entry : paramSpecs.entrySet()) {
                        String paramName = entry.getKey()
                        def spec = entry.getValue()
                        String type = spec.type as String
                        String description = spec.description as String
                        boolean required = spec.required != null && spec.required
                        Object defaultValue = spec.default

                        DynamicProperty prop = new DynamicProperty(paramName, description, String.class)
                        prop.setAttribute(BeanInfoConstants.CAN_BE_NULL, !required)
                        boolean isList = type.startsWith("Array")

                        if ("File".equals(type) || type.startsWith("Array")) {
                            // File parameter: grouped dropdown (one group per top-level folder).
                            // Uses the GroupedSelectPropertyInput custom component via
                            // extraAttrs inputType. Display: relative path; the stored
                            // selected value is the absolute path.
                            String[][] tags = buildFileTags(projectFiles, workflowFiles, projectName)
                            prop.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, tags)
                            // Array parameters accept multiple files
                            if (isList) {
                                prop.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true)
                            }
                            // If there's a default path, resolve it to an absolute path
                            // (whether given as a relative path, or already absolute)
                            Map<String, String> allFiles = new LinkedHashMap<>(projectFiles)
                            allFiles.putAll(workflowFiles)
                            List<String> defaultAbs = resolvePaths(allFiles, defaultValue)
                            if (!defaultAbs.isEmpty()) {
                                prop.setValue(isList ? defaultAbs.toArray(new String[0]) : defaultAbs.get(0))
                            }
                        } else {
                            // String or Int: plain text field
                            if (defaultValue != null) {
                                prop.setValue(defaultValue.toString())
                            }
                        }

                        params.add(prop)
                    }
                }
            }
        }

        // Apply preset values (from previous step if user went back)
        params = DpsUtils.setValues(params, presetValues)

        return params
    }

    /**
     * Collect files from file_info for the given project.
     * Returns Map: path relative to the project samples folder -> absolute path.
     */
    private Map<String, String> getProjectFiles(long prjId) throws Exception {
        Map<String, String> result = new LinkedHashMap<>()
        def prjRow = db.recordWithParams("SELECT name FROM projects WHERE id = ?", prjId)
        String projectName = prjRow?.getString("name")
        if (projectName == null) return result

        String repoPath = repo.getRepositoryPath()
        if (repoPath == null) return result

        DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(projectName )
        if (projectPath.exists()) {
            def rows = db.list("SELECT path FROM file_info WHERE project = ${prjId} ORDER BY path")
            for (def row : rows) {
                String filePathStr = row.$path
                if (filePathStr == null || filePathStr.isBlank())
                    continue

                DataElementPath filePath = DataElementPath.create(filePathStr)
                // filePathStr is a complete repo-root-relative path (e.g. "projects/<prj>/...").
                if (filePath.exists()) {
                    // Resolve the absolute file via the parent collection (single walk).
                    File f = filePath.getParentPath().getDataElement(GenericFileDataCollection.class).getFile(filePath.getName())
                    if (f != null) {
                        def relativePath = filePath.getPathDifference(projectPath );
                        result.put(relativePath, f.getAbsolutePath())
                    }
                }
            }
        }
        return result
    }

    /**
     * Collect workflow reference files (genome databases, etc.) stored in file_info
     * as folders (entity = 'workflow_info'). These are supporting data shared across
     * all workflows, so the query is NOT filtered by the current workflow — but the
     * folders belonging to the selected workflow are listed first.
     * Returns Map: path relative to the repository root -> absolute path.
     */
    private Map<String, String> getWorkflowFiles(long workflowId) {
        Map<String, String> result = new LinkedHashMap<>()
        // Folders for the selected workflow first, then the rest (shared supporting data).
        def rows = db.list("SELECT fi.path FROM file_info fi " +
                "INNER JOIN file_types ft ON ft.id = fi.filetype " +
                "WHERE fi.entity='workflow_info' AND ft.suffix = 'folder' " +
                "ORDER BY (fi.entityID = ${workflowId}) DESC, fi.path" )
        for (def row : rows) {
            String filePathStr = row.$path
            if (filePathStr == null || filePathStr.isBlank())
                continue
            DataElementPath filePath = DataElementPath.create(filePathStr)
            if (filePath.exists()) {
                GenericFileDataCollection folder = filePath.getDataElement(GenericFileDataCollection.class)
                RepositoryFileUtils.collectItemsRecursive( folder, result, true );
            }
        }
        return result
    }

    /**
     * Build a tag list for File-type parameters using grouped options.
     * Rows are [value, label] pairs:
     *   - File row:     [absolutePath, relativePath]
     *   - Group header: ["", "@@group@@<groupName>"]  (recognized by the patched SelectPropertyInput)
     *
     * Each top-level folder becomes a react-select group. The group label
     * includes the section context: "PrjName / folder" for project files,
     * "Genome / folder" for workflow reference files.
     */
    private static String[][] buildFileTags(Map<String, String> projectFiles,
            Map<String, String> workflowFiles,
            String projectName) {
        List<String[]> tags = new ArrayList<>()
        if (projectFiles.isEmpty() && workflowFiles.isEmpty()) {
            tags << [
                "",
                "@@group@@No files available"
            ]
            return tags as String[][]
        }
        String prjPrefix = (projectName != null && !projectName.isBlank()) ? projectName : "Project"
        appendGroupedTags(tags, projectFiles, prjPrefix)
        appendGroupedTags(tags, workflowFiles, "Genome")
        return tags as String[][]
    }

    private static void appendGroupedTags(List<String[]> tags,
            Map<String, String> files,
            String sectionPrefix) {
        if (files.isEmpty()) return
            // Group by first path segment; "(root)" for files at the top level
            Map<String, List<String[]>> groups = new LinkedHashMap<>()
        files.each { rel, abs ->
            String group = rel.contains('/') ? rel.substring(0, rel.indexOf('/')) : "(root)"
            groups.computeIfAbsent(group, { _ -> new ArrayList<String[]>() }) << [abs, rel]
        }
        groups.each { groupName, entries ->
            tags << [
                "",
                "@@group@@${sectionPrefix} / ${groupName}"
            ]
            entries.each { tags << it }
        }
    }

    /**
     * Resolve a default value against the file maps. Accepts a single path
     * or a list of paths (Array parameters), each given either as a relative
     * path (a map key) or as an absolute path (a map value).
     * Returns the list of absolute paths; empty if nothing matched.
     */
    private static List<String> resolvePaths(Map<String, String> files, Object defaultValue) {
        List<String> result = new ArrayList<>()
        if (defaultValue == null) return result
        List<?> defaults = defaultValue instanceof Collection ? defaultValue : [defaultValue]
        for (def d : defaults) {
            if (d == null) continue
                String p = d.toString()
            if(files.containsKey(p ))
                result << files.get(p)
            else {
                def abspaths = files.findAll {
                    it.value == p
                }
                def abs = abspaths.isEmpty()?null: abspaths.values().first()
                if (abs != null) {
                    result << abs
                }
            }
        }
        return result
    }

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        // Collect all filled parameters back into JSON to pass as nextflow json file
        // Collect all filled parameters back to pass as nextflow json file.
        // Multi-select (Array) parameters arrive as String[] — serialize them
        // as JSON arrays, everything else as a plain string.
        Map<String, Object> workflowParams = new LinkedHashMap<>()
        for (def prop : params.propertyIterator()) {
            String key = prop.getName()
            Object value = params.getValue(key)
            if (key.startsWith("_"))
                continue
            if (value == null)
                continue
            if (value instanceof Object[]) {
                workflowParams.put(key, (value as Object[]) as List)
            } else {
                workflowParams.put(key, value.toString())
            }
        }

        Long workflowId = Long.parseLong(params.getValue("_workflowInfoId_").toString())
        Long projectId = Long.parseLong(params.getValue("_prjID_").toString())

        String repoPath = repo.getRepositoryPath()

        def prj = database.getEntity( "projects" ).get( projectId )
        DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(prj.$name );
        GenericFileDataCollection prjDc = projectPath.getDataCollection();

        DataElementPath results = projectPath.getChildPath("results" );
        GenericFileDataCollection resultsDc = DataCollectionUtils.createSubCollection(results);


        def wfRow = db.recordWithParams( "SELECT ft.suffix, fi.path, wi.params FROM workflow_info wi " +
                "INNER JOIN file_info fi ON fi.id = wi.file_info INNER JOIN file_types ft on ft.id = fi.fileType WHERE wi.id = ?", workflowId)
        if(wfRow == null) {
            setResult(OperationResult.error("Workflow not specified"))
            return
        }
        String wfPath = wfRow?.getString("path");
        if(wfPath == null || wfPath.isBlank()) {
            setResult(OperationResult.error("Workflow file is not set"))
            return
        }

        DataElementPath workflowPath = DataElementPath.create( wfPath);
        if(!workflowPath.exists()) {
            setResult(OperationResult.error("Workflow not found"))
            return
        }

        Path outputDir = TempFiles.getTempDirectory().toPath()
        String workflowName = workflowPath.getName();
        GenericFileDataCollection wfParent = workflowPath.getParentPath().getDataElement(GenericFileDataCollection.class )
        File workflowFile  = wfParent.getFile(workflowName );
        String nextFlowScript = null;
        String suffix = wfRow.getString("suffix")

        def workflowRunId = nf.insertWorkflowRun ( projectId.intValue(), workflowId.intValue())
        String runResultsSubdir = "workflow_run_${workflowRunId}"
        DataElementPath resultsPath = results.getChildPath(runResultsSubdir)
        if(!resultsPath.exists()) {
            DataCollectionUtils.createSubCollection(resultsPath);
        }
        File resultsDir = resultsDc.getFile(runResultsSubdir);
        db.update("UPDATE workflow_runs SET comment = ? WHERE id = ?", runResultsSubdir, workflowRunId)

        boolean useDocker = false
        String useDockerStr = db.getString( "SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='docker_nextflow'" )
        if(useDockerStr != null) {
            useDocker = Boolean.parseBoolean(useDockerStr )
        }

        WorkflowSettings settings = new WorkflowSettings()
        settings.setOutputPath(results )
        settings.setUseDocker(useDocker)
        settings.getNextflowSettings().setPublishOutput("copy" )


        try {
            if(".nf".equals( suffix )) {
                nextFlowScript = ApplicationUtils.readAsString(workflowFile);
                //outdir
                settings.getNextflowSettings().setPublishDir(resultsDir.getAbsolutePath() )
                workflowParams.put("outdir", resultsDir.getAbsolutePath())
            }
            else if(".wdl".equals( suffix )){
                Map<String, Diagram> diagrams = WDLImporter.loadWDLDiagrams(workflowFile.toPath());
                generateNextflow(diagrams, outputDir, resultsDir.toPath(), settings);
                File nfWorkflowFile = outputDir.resolve(workflowName + ".nf").toFile();
                nextFlowScript = ApplicationUtils.readAsString(nfWorkflowFile);
            }
            else {
                resultsPath.remove()
                db.update("UPDATE workflow_runs SET status = ? WHERE id = ?", "failed", workflowRunId)
                setResult(OperationResult.error("Unknown workflow type"))
                return
            }

            def serverUrl = request.getServerUrl()


            String referer = null
            if( request != null && request.getRawRequest() != null ) {
                referer = request.getRawRequest().getHeader( "referer" )
            }
            if(referer != null) {
                serverUrl = referer.replaceAll('/$', '')
            }

            def serverDockerUrl = db.getString( "SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='docker_server_url'" );
            if( serverDockerUrl != null ) {
                serverUrl = serverDockerUrl
            }

            def towerAddress = serverUrl+"/nf"



            GeneSpaceContext context = new GeneSpaceContext(repo.getProjectsPath(), repo.getWorkflowsPath(), repo.getGenomePath(), outputDir)

            // Pass the project results folder to the workflow. Hand-written .nf
            // scripts declare `params.resultsDir` and reference it in publishDir;
            // generated (WDL) scripts already bake it in via setPublishDir.
            workflowParams.put("resultsDir", resultsDir.getAbsolutePath())

            String json = JsonOutput.toJson(workflowParams)
            File jsonFile = outputDir.resolve("parameters.json" ).toFile();
            ApplicationUtils.writeString(jsonFile, json );

            NextFlowRunner.generateFunctions(outputDir.toAbsolutePath().toString())


            NextFlowRunner.runNextFlow("${workflowRunId}", workflowName, nextFlowScript, false, settings, towerAddress, context, jsonFile.toString(), true)

            setResult(OperationResult.finished("Workflow ${workflowName} started"))
        }
        catch ( Exception e ) {
            // A workflow_runs row was already created; if anything from script
            // generation through runNextFlow fails, mark the run failed and drop
            // the partially-created results folder so it doesn't linger.
            log.error( "Workflow ${workflowName} (run ${workflowRunId}) failed to start: " + e.getMessage() )
            if( resultsPath.exists() ) {
                resultsPath.remove()
            }
            db.update("UPDATE workflow_runs SET status = ? WHERE id = ?", "failed", workflowRunId)
            setResult(OperationResult.error("Failed to start workflow ${workflowName}: ${e.getMessage()}"))
        }
    }



    protected static void generateNextflow(Map<String, Diagram> diagrams, Path outputFolderPath, Path resultsFolderPath, WorkflowSettings settings) {
        NextFlowGenerator gen = new NextFlowGenerator();
        gen.setPublishDir(resultsFolderPath.toAbsolutePath().toString());
        gen.setResultPath(outputFolderPath.toAbsolutePath().toString())
        gen.setNextflowSettings(settings.getNextflowSettings())
        for(Entry<String, Diagram> entry : diagrams) {
            Path outFilePath = outputFolderPath.resolve(entry.getKey() + ".nf");
            String nextFlow = gen.generate(entry.getValue());

            outFilePath.toFile().withOutputStream{ fos ->
                ApplicationUtils.writeString(fos, nextFlow)
            }
        }
    }
}
