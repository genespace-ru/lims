package operations;

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.logging.Logger
import java.util.regex.Pattern

import javax.inject.Inject

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.model.Base64File
import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONObject

import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport
import biouml.model.Diagram
import biouml.plugins.wdl.WorkflowSettings

import com.developmentontheedge.beans.BeanInfoConstants
import groovy.json.JsonException
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletResponse
import ru.biosoft.access.core.DataElementPath
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.nextflow.NextflowService
import ru.biosoft.server.servlets.webservices.BiosoftWebResponse
import ru.biosoft.server.servlets.webservices.JSONResponse
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.util.RepositoryFileUtils
import ru.biosoft.util.TempFiles
import ru.biosoft.util.archive.ArchiveFactory
import biouml.plugins.wdl.diagram.WDLImporter

public class ImportWorkflowOperation extends GOperationSupport {

    private static final Logger log = Logger.getLogger( ImportWorkflowOperation.class.getName() )

    @Inject
    private RepositoryManager repo;
    @Inject
    protected NextflowService nf;
    Map<String, Object> presets

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        presets = presetValues
        params.workflowFile = [ TYPE: java.io.File, DISPLAY_NAME: "Workflow file (.nf, .wdl or archive for complex workflow)" ]
        params.mainWorkflowName = [TYPE: String, CAN_BE_NULL: true, DISPLAY_NAME: "Main workflow name in case of archive"]
        params.workflowDescription = [TYPE: String, CAN_BE_NULL: true, DISPLAY_NAME: "Description"]
        params.inputsFile = [ TYPE: Base64File, CAN_BE_NULL: true, DISPLAY_NAME: "Inputs JSON (optional file with inputs in json format)" ]
        def sample = '''{"tumor_r1_fastq": {"type": "File"}, 
"tumor_sample_name": {"type": "String", 
                      "default": "BRCA_001", 
                      "description": "Sample name"}}'''
        params.inputsFile_sample = [TYPE: String,
            value: sample,
            DISPLAY_NAME : "JSON example",
            CAN_BE_NULL: true,
            READ_ONLY: true,
            EXTRA_ATTRS: [
                ["inputType", "textArea"],
                ["rows", "5"]
            ] as String[][]]

        DpsUtils.setValues(params, presetValues)
        Object inputs = params.getValue("inputsFile")
        if (inputs instanceof Base64File) {
            try {
                def parsed = new JsonSlurper().parseText(new String(inputs.data, java.nio.charset.StandardCharsets.UTF_8))
                validateInputsSpec(parsed)
            } catch (Exception ex) {
                validator.setError(params.inputsFile, "Incorrect JSON: ${ex.message}")
                return params
            }
        }
        return params
    }

    private static void validateInputsSpec(def parsed) {
        if (!(parsed instanceof Map))
            throw new JsonException("Expected JSON-object with parameters")
        for (def e : parsed.entrySet()) {
            def spec = e.value
            if (!(spec instanceof Map))
                throw new JsonException("Parameter '${e.key}': expected object with properties")
            String type = spec.type?.toString()
            if (type == null || type.isBlank())
                throw new JsonException("Parameter '${e.key}': 'type' not specified")
        }
    }

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        // workflowFile is a java.io.File upload control: the DPS value is the field
        // name, and the actual bytes are an Apache Commons FileItem staged in a temp
        // file by the servlet container. Unwrap it via the inherited getFileItem().
        FileItem workflowItem = getFileItem(params.getValue("workflowFile"))
        if (workflowItem == null || workflowItem.isFormField()) {
            setResult(OperationResult.error("Workflow file is not specified"))
            return
        }
        // workflowItem.name -> original file name; copy to a named temp file before
        // handing the path to WDLImporter / reading the .nf (mirrors LoadProject).
        File tempDir = TempFiles.getTempDirectory()
        def name = workflowItem.name
        File workflowTmp = new File(tempDir, name)
        workflowItem.inputStream.withCloseable { ins ->
            workflowTmp.withOutputStream { outs -> IOUtils.copy(ins,outs) }
        }

        Path workflows = repo.getWorkflowsPath()

        Path workflowPath = null;
        if( ArchiveFactory.getArchiveFile( workflowTmp ) != null ) {
            //it is an archive — unpack and pick the main workflow
            boolean copyArchive = false
            File unpackedFolder = TempFiles.dir( name + "_unpacked" );
            Path dir = unpackedFolder.toPath();
            try {
                ArchiveFactory.unpack( workflowTmp, unpackedFolder );


                List<String> files = RepositoryFileUtils.collectWorkflowFiles( dir, dir );
                if(files.isEmpty()) {
                    setResult(OperationResult.error("Archive does not contain any workflow file"))
                    return
                }

                String mainName = params.getValue("mainWorkflowName")?.toString()
                if( mainName == null || mainName.isBlank() ) {
                    if( files.size() == 1 ) {
                        //single workflow in the archive — no need to specify
                        workflowPath = dir.resolve( files.iterator().next() )
                        copyArchive = true
                    }
                    else {
                        setResult( OperationResult.error(
                                "Archive contains ${files.size()} workflows: " +
                                files.collect { it.substring(it.lastIndexOf('/') + 1) }.join(", ") +
                                ". Specify the main workflow name." ) )
                        return
                    }
                }
                else {
                    //match by file name, with or without the .wdl/.nf extension, case-insensitive
                    String normalized = mainName.trim().toLowerCase()
                    if( !normalized.endsWith( ".wdl" ) && !normalized.endsWith( ".nf" ) ) {
                        //try .wdl and .nf
                        List<String> matches = files.findAll {
                            String fileName = it.substring(it.lastIndexOf('/') + 1).toLowerCase()
                            fileName.equals( normalized + ".wdl" ) || fileName.equals( normalized + ".nf" )
                        }
                        if( matches.isEmpty() ) {
                            setResult( OperationResult.error(
                                    "Workflow '${mainName}' not found in archive. Available: " +
                                    files.collect { it.substring(it.lastIndexOf('/') + 1) }.join(", ") ) )
                            return
                        }
                        //several files with the same name — take the first
                        workflowPath = dir.resolve( matches.first() )
                        copyArchive = true
                    }
                    else {
                        List<String> matches = files.findAll {
                            it.substring(it.lastIndexOf('/') + 1).equalsIgnoreCase( mainName.trim() )
                        }
                        if( matches.isEmpty() ) {
                            setResult( OperationResult.error(
                                    "Workflow '${mainName}' not found in archive. Available: " +
                                    files.collect { it.substring(it.lastIndexOf('/') + 1) }.join(", ") ) )
                            return
                        }
                        //several files with the same name — take the first
                        workflowPath = dir.resolve( matches.first() )
                        copyArchive = true
                    }
                }
            }
            catch (IOException e) {
                setResult(OperationResult.error("There was an error during unpack: " + e.getMessage() ))
                return
            }
            catch (Exception e) {
                setResult(OperationResult.error("There was an error: " + e.getMessage() ))
                return
            }
            if(copyArchive) {
                //name the folder after the archive file (e.g. brca.tar.gz -> brca),
                //stripping archive extensions, not the workflow file's extension
                def nameNoExt = RepositoryFileUtils.stripArchiveExtensions(name)
                Path newDir = workflows.resolve(nameNoExt )
                if(Files.exists(newDir)) {
                    setResult(OperationResult.error("Folder $nameNoExt alreay exists in workflows. Rename the archive or use already existing workflow."))
                    return
                }
                RepositoryFileUtils.copyDirOverwrite(dir, newDir)
                workflowPath = newDir.resolve(dir.relativize(workflowPath ))
            }
        }
        else {
            //not an archive — a plain .wdl or .nf file
            String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase()
            if( ext != "wdl" && ext != "nf" ) {
                setResult(OperationResult.error("Workflow file must be .wdl or .nf (or an archive)"))
                return
            }
            workflowPath = workflows.resolve(workflowTmp.getName() )
            Files.copy(workflowTmp.toPath(), workflowPath, StandardCopyOption.REPLACE_EXISTING)
        }

        if(workflowPath == null) {
            setResult(OperationResult.error("Workflow file is not specified"))
            return
        }
        String workflowName = workflowPath.getFileName()

        String ext = workflowName.substring(workflowName.lastIndexOf('.')).toLowerCase()

        DataElementPath workflowsPath = repo.getWorkflowsCollection().getCompletePath()
        Path pathRelative = workflows.relativize( workflowPath )
        String[] parts = pathRelative.toString().split( Pattern.quote( File.separator ) )
        DataElementPath workflowDePath = workflowsPath.getChildPath( parts )


        Long typeID = db.oneLong( "SELECT id FROM file_types WHERE suffix = ?", ext )
        if(typeID == null) {
            String descr = ext.equals( ".wdl" ) ? "Script in Workflow Description Language." : "Nextflow workflow script";
            typeID = database.file_types << [suffix: ext, description: descr]
        }

        def fileExistedId = db.oneLong( "SELECT id FROM file_info WHERE fileType = ? AND fileName =? AND path = ? and entity=?",
                typeID, workflowName, workflowDePath.toString(), "workflow_info" )
        Long fileId = null
        if(fileExistedId != null) {
            setResult(OperationResult.error("Workflow $workflowName alredy exists."))
            return
        }
        else {
            database.file_info << [fileType: typeID, fileName: workflowName,
                path: workflowDePath.toString(), size: Files.size(workflowPath ), project: -1,
                entity: "workflow_info"]
            fileId = db.oneLong( "SELECT id FROM file_info WHERE fileType = ? AND fileName =? AND path = ? and entity=?",
                    typeID, workflowName, workflowDePath.toString(), "workflow_info" );
        }

        def workflowId = database.workflow_info << [file_info: fileId, title:workflowName, description:params.getValue("workflowDescription")?.toString()]
        db.update("UPDATE file_info SET entityID = ? WHERE id = ?", workflowId, fileId)
        // inputsFile stays a Base64File: in-memory bytes, parse directly.
        Object inputs = params.getValue("inputsFile")
        String inputsJson = inputs instanceof Base64File
                ? new String(inputs.data, java.nio.charset.StandardCharsets.UTF_8)
                : null

        if(inputsJson == null) {
            inputsJson = readParametersFromWorkflow(workflowPath)
        }
        if(inputsJson != null) {
            db.updateRaw("UPDATE workflow_info SET params = ?::json WHERE id = ?", inputsJson, workflowId)
        }
        setResult(OperationResult.finished("Workflow ${workflowName} imported"))
    }

    private String readParametersFromWorkflow(Path workflowPath) {
        String workflowName = workflowPath.getFileName()
        String format = workflowName.substring(workflowName.lastIndexOf('.') + 1)
        switch (format?.toLowerCase()) {
            case "nf":
                break
            case "wdl":
                Map<String, Diagram> diagrams = WDLImporter.loadWDLDiagrams(workflowPath)
                Diagram diagram = diagrams.get(workflowName )
                WorkflowSettings settings = new WorkflowSettings()
                settings.initParameters( diagram )
                File tempDir = TempFiles.getTempDirectory()
                File jsonFile = settings.generateParametersJSON(tempDir.getAbsolutePath() )
                return ApplicationUtils.readAsString(jsonFile )
            default:
                return null
        }
    }
}
