package operations

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.logging.Logger

import javax.inject.Inject

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.FrontendActions
import com.developmentontheedge.be5.server.operations.support.GOperationSupport

import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.BeanInfoConstants

import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.IOUtils
import ru.biosoft.access.core.DataCollection
import ru.biosoft.access.core.DataElementPath
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.util.TempFiles
import ru.biosoft.util.archive.ArchiveFactory
import ru.biosoft.util.archive.ArchiveEntry
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.lims.util.FileTypeUtils

import ru.biosoft.access.DataCollectionUtils

public class ImportFilesToProject extends GOperationSupport {

    private static final Logger log = Logger.getLogger(ImportFilesToProject.class.getName())

    @Inject
    private RepositoryManager repo

    Map<String, Object> presets

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        presets = presetValues

        def prjId = context.getParams().getOrDefault("___prjID", presetValues.get("___prjID"))
        if (prjId != null) {
            prjId = Long.valueOf(prjId.toString())
        }

        params.projectId = [value: prjId,
            DISPLAY_NAME: "Project", TYPE: Long,
            HIDDEN: true, CAN_BE_NULL: false]

        // Display the project name as a read-only label
        def projectName = prjId ? db.oneString("SELECT name FROM projects WHERE ID = ?", prjId) : ""
        params.projectName = [value: projectName,
            DISPLAY_NAME: "Importing to project",
            TYPE: String,
            HIDDEN: false,
            READ_ONLY: true,
            CAN_BE_NULL: true]

        params.files = [TYPE: java.io.File,
            DISPLAY_NAME: "Files to import",
            CAN_BE_NULL: false]
        params.files.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true)

        params.comment = [value: "", TYPE: String,
            DISPLAY_NAME: "Comment",
            CAN_BE_NULL: true]

        params = DpsUtils.setValues(params, presetValues)

        return params
    }

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()
        Long projectId = params.$projectId
        if (projectId != null) {
            projectId = Long.valueOf(projectId.toString())
        }
        String comment = params.$comment?.toString() ?: ""

        if (projectId == null) {
            setResult(OperationResult.error("Project is not specified"))
            return
        }

        String repoPath = repo.getRepositoryPath()
        if (repoPath == null) {
            setResult(OperationResult.error("Repository is not available"))
            return
        }

        DataElementPath parentPath = DataElementPath.create(repoPath)

        // Find the project's DataCollection by name
        def projectName = db.oneString("SELECT name FROM projects WHERE ID = ?", projectId)
        if (projectName == null) {
            setResult(OperationResult.error("Project not found"))
            return
        }

        DataElementPath projectPath = parentPath.getChildPath(projectName)
        if (!DataElementPath.create(projectPath.toString()).exists()) {
            setResult(OperationResult.error("Project directory does not exist. Create the project via LoadProject first, or create the directory manually."))
            return
        }

        DataCollection samples = DataCollectionUtils.createSubCollection(projectPath.getChildPath("samples"), true)

        Object[] files = params.$files
        List<String> errors = []
        int imported = 0
        // Collect paths of files actually imported (newly created on disk)
        List<String> importedPaths = []

        for (Object fileObj in files) {
            FileItem fileItem = getFileItem(fileObj)
            if (fileItem == null || fileItem.isFormField()) {
                errors.add("Invalid file upload")
                continue
            }

            String fileName = fileItem.name
            File tempFile = new File(TempFiles.getTempDirectory(), fileName)
            try {
                fileItem.inputStream.withCloseable { ins ->
                    tempFile.withOutputStream { outs ->
                        IOUtils.copy(ins, outs)
                    }
                }

                // Handle archives: use RepositoryManager's built-in unpacker
                def archiveFile = ArchiveFactory.getArchiveFile(tempFile)
                if (archiveFile != null) {
                    RepositoryManager.importArchiveFile(tempFile, samples, importedPaths)
                    imported++
                } else {
                    // Single file: copy to samples directory
                    File destFile = RepositoryManager.getChildFile(samples, fileName)
                    ApplicationUtils.copyFile(destFile, tempFile, null)
                    importedPaths.add(samples.getCompletePath().getChildPath(fileName).toString())
                    imported++
                }
            } catch (Exception e) {
                errors.add("Error importing ${fileName}: ${e.message}")
            } finally {
                tempFile.delete()
            }
        }

        // Create file_info records only for files that were just imported
        for (String pathStr in importedPaths) {
            def existing = db.oneLong(
                    "SELECT ID FROM file_info WHERE path = ? AND project = ?",
                    pathStr, projectId)
            if (existing != null) continue

                def typeID = FileTypeUtils.getOrCreateTypeId(db, new File(pathStr));
            database.file_info << [
                fileName: new File(pathStr).getName(),
                fileType: typeID,
                path: pathStr,
                size: new File(pathStr).length(),
                project: projectId,
                entity: "sample",
                entityID: null,
                comment: comment
            ]
        }

        if (errors.isEmpty()) {
            setResult(OperationResult.finished("Imported ${imported} file(s)"))
        } else {
            setResult(OperationResult.finished("Some files failed: " + String.join("\n", errors)))
        }
        // No FrontendActions: the be5 framework auto-closes the modal on a bare
        // FINISHED result (forms.js) and shows the success alert, leaving the user
        // on the project card. The card's "Файлы" tab refresh is handled on the
        // frontend (ProjectCardTableBox listens for the executeFrontendActions bus
        // event and re-loads the active tab into the navs document).
    }
}
