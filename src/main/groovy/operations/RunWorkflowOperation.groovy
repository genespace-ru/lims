package operations;

import java.nio.file.Path
import java.util.Map.Entry

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
import ru.biosoft.util.TempFiles

import biouml.plugins.wdl.WorkflowSettings

public class RunWorkflowOperation extends GOperationSupport {

    @Inject
    private RepositoryManager repo;
    @Inject
    protected NextflowService nf;
    Map<String, Object> presets

    // -----------------------------------------------------------------
    // Step 1-2: fetch workflows from workflow_info + file_info,
    //           return them as a dropdown so the user picks one.
    // Step 4:   read workflow_info.params JSON, build a DynamicPropertySet
    //           with typed fields (String/File/Int) for each parameter.
    // Step 5:   File/String parameters show a dropdown of files from the
    //           current project's file repository.
    // -----------------------------------------------------------------

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        presets = presetValues
        DynamicPropertySetSupport params = new DynamicPropertySetSupport()

        // --- 1. Resolve the current project from the context ------------
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

        // --- 2. Load all workflows into a dropdown ----------------------
        // Uses the custom selection view that JOINs workflow_info with file_info
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

        // --- 3. If a workflow was selected, build parameter form --------
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
                    // project files and workflow files. Maps: display label
                    // (path relative to the repository root) -> absolute path,
                    // which is what nextflow receives.
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
                            // File parameter: single dropdown grouped into two sections —
                            // "Project files" and "Workflow files" — separated by a
                            // non-selectable label row (a tag with an empty value).
                            // Display: relative path; the stored/selected value
                            // is the absolute path.
                            String[][] tags = buildFileTags(projectFiles, workflowFiles)
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
     * Returns Map: path relative to the project repository -> absolute path.
     */
    private Map<String, String> getProjectFiles(long prjId) throws Exception {
        Map<String, String> result = new LinkedHashMap<>()
        def prjRow = db.recordWithParams("SELECT name FROM projects WHERE id = ?", prjId)
        String projectName = prjRow?.getString("name")
        if (projectName == null) return result

        String repoPath = repo.getRepositoryPath()
        if (repoPath == null) return result

        DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(projectName )
        GenericFileDataCollection prjDc = projectPath.getDataCollection()
        if (projectPath.exists()) {
            def rows = db.list("SELECT path FROM file_info WHERE project = ${prjId} ORDER BY path")
            for (def row : rows) {
                String filePathStr = row.$path
                if (filePathStr == null || filePathStr.isBlank())
                    continue

                DataElementPath filePath = DataElementPath.create(filePathStr)
                //Probably not fully correct check
                if (filePath.exists()) {
                    File f = filePath.getParentPath().getDataElement(GenericFileDataCollection.class).getFile(filePath.getName())
                    if (f != null && f.exists()) {
                        result.put(filePathStr, f.getAbsolutePath())
                    }
                }
            }
        }
        return result
    }

    /**
     * Collect workflow reference files (genome databases, etc.) stored in file_info
     * as folders (entity = 'workflow_info') for the given workflow.
     * Returns Map: path relative to the repository root -> absolute path.
     */
    private Map<String, String> getWorkflowFiles(long workflowId) {
        Map<String, String> result = new LinkedHashMap<>()
        def rows = db.list("SELECT fi.path FROM file_info fi " +
                "INNER JOIN file_types ft ON ft.id = fi.filetype " +
                "WHERE fi.entity='workflow_info' AND fi.entityID=${workflowId} AND ft.suffix = 'folder' ORDER BY fi.path")
        for (def row : rows) {
            String filePathStr = row.$path
            if (filePathStr == null || filePathStr.isBlank())
                continue
            DataElementPath filePath = DataElementPath.create(filePathStr)
            if (filePath.exists()) {
                GenericFileDataCollection folder = filePath.getDataElement(GenericFileDataCollection.class)
                collectItemsRecursive(folder, result)
            }
        }
        return result
    }


    private void collectItemsRecursive(GenericFileDataCollection folder, Map<String, String> result) {
        if (folder == null || !folder.getCompletePath().exists()) return
            for (String name : folder.getNameList()) {
                DataElementPath p = folder.getCompletePath().getChildPath(name)
                File f = folder.getFile(name)
                if (f != null && f.exists()) {
                    result.put(p.toString(), f.getAbsolutePath())
                }
                // descend into subfolders
                def child = p.getDataElement()
                if (child instanceof GenericFileDataCollection && p.exists())
                    collectItemsRecursive(child, result)
            }
    }

    /**
     * Build a tag list for File-type parameters: a single flat list with two sections,
     * "Project files" and "Workflow files", separated by a non-selectable label row
     * (empty value). The frontend renders a tag with an empty value as a disabled
     * label, so the row acts as a visual separator.
     * Tags are [absolutePath, relativePath] — the value is the absolute path,
     * the display label is the relative path.
     */
    private static String[][] buildFileTags(Map<String, String> projectFiles, Map<String, String> workflowFiles) {
        List<String[]> tags = new ArrayList<>()
        if (projectFiles.isEmpty() && workflowFiles.isEmpty()) {
            tags << ["(no files)", "(no files)"]
            return tags as String[][]
        }
        if (!projectFiles.isEmpty()) {
            tags << ["", "----Project files----"]
            projectFiles.each { rel, abs ->
                tags << [abs, rel]
            }
        }
        if (!workflowFiles.isEmpty()) {
            tags << ["", "----Genome files----"]
            workflowFiles.each { rel, abs ->
                tags << [abs, rel]
            }
        }
        return tags as String[][]
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
            String abs = files.get(p)
                    ?: files.findAll {
                        it.value == p
                    }.values().first()
            if (abs != null) {
                result << abs
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
        if(!results.exists()) {
            DataCollectionUtils.createSubCollection(results);
        }
        File resultsDir = prjDc.getFile("results");

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
        if(suffix.equals(".nf" )) {
            nextFlowScript = ApplicationUtils.readAsString(workflowFile);
            //TODO: pass params.outdir in nextflow config parameters
        }
        else if(suffix.equals(".wdl" )){
            Map<String, Diagram> diagrams = WDLImporter.loadWDLDiagrams(workflowFile.toPath());
            generateNextflow(diagrams, outputDir, resultsDir.toPath());
            File nfWorkflowFile = outputDir.resolve(workflowName + ".nf").toFile();
            nextFlowScript = ApplicationUtils.readAsString(nfWorkflowFile);
        }
        else {
            setResult(OperationResult.error("Unknown workflow type"))
            return
        }

        def workflowRunId = nf.insertWorkflowRun ( projectId.intValue(), workflowId.intValue())

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

        boolean useDocker = false
        String useDockerStr = db.getString( "SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='docker_nextflow'" )
        if(useDockerStr != null) {
            useDocker = Boolean.parseBoolean(useDockerStr )
        }

        GeneSpaceContext context = new GeneSpaceContext(repo.getProjectsPath(), repo.getWorkflowsPath(), repo.getGenomePath(), outputDir)

        // Pass the project results folder to the workflow. Hand-written .nf
        // scripts declare `params.resultsDir` and reference it in publishDir;
        // generated (WDL) scripts already bake it in via setPublishDir.
        workflowParams.put("resultsDir", resultsDir.toAbsolutePath().toString())

        String json = JsonOutput.toJson(workflowParams)
        File jsonFile = outputDir.resolve("parameters.json" ).toFile();
        ApplicationUtils.writeString(jsonFile, json );

        NextFlowRunner.generateFunctions(outputDir.toAbsolutePath().toString())

        WorkflowSettings settings = new WorkflowSettings()
        settings.setUseDocker(useDocker)
        settings.getNextflowSettings().setPublishOutput(useDockerStr )
        NextFlowRunner.runNextFlow("${workflowRunId}", workflowName, nextFlowScript, false, settings, towerAddress, context, jsonFile.toString())

        setResult(OperationResult.finished("Workflow ${workflowName} started"))
    }



    protected static void generateNextflow(Map<String, Diagram> diagrams, Path outputFolderPath, Path resultsFolderPath) {
        NextFlowGenerator gen = new NextFlowGenerator();
        gen.setPublishDir(resultsFolderPath.toAbsolutePath().toString());
        for(Entry<String, Diagram> entry : diagrams) {
            Path outFilePath = outputFolderPath.resolve(entry.getKey() + ".nf");
            String nextFlow = gen.generate(entry.getValue());

            outFilePath.toFile().withOutputStream{ fos ->
                ApplicationUtils.writeString(fos, nextFlow)
            }
        }
    }
}
