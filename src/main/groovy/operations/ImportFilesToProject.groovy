package operations

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.logging.Logger

import javax.inject.Inject

import com.developmentontheedge.be5.database.DbService
import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.FrontendActions
import com.developmentontheedge.be5.server.operations.support.GOperationSupport

import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.DynamicProperty
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

        // Sample selector: a project's samples plus a "no sample" option.
        // The chosen sample is stored in file_info.entity='samples'/entityID;
        // VCF files need this to be importable (snv_ tables are named by sample).
        List<String[]> sampleTagList = [ [ "", localize("{{{NoSample}}}") ] as String[] ]
        if (prjId != null) {
            def sampleRows = db.listWithParams("SELECT ID, title FROM samples WHERE project = ? ORDER BY title", prjId)
            for (def s : sampleRows) {
                sampleTagList << [ s.$ID.toString(), s.$title?.toString() ] as String[]
            }
        }
        String[][] sampleTags = new String[sampleTagList.size()][2]
        for (int i = 0; i < sampleTagList.size(); i++) {
            sampleTags[i] = sampleTagList.get(i)
        }
        DynamicProperty sampleProp = new DynamicProperty("sampleId", "Sample", String.class)
        sampleProp.setAttribute(BeanInfoConstants.TAG_LIST_ATTR, sampleTags)
        sampleProp.setAttribute(BeanInfoConstants.CAN_BE_NULL, true)
        params.add(sampleProp)
        // Small-letters hint (like LoadProject's sequencesFile label).
        dpsHelper.addLabelRaw(params, localize("{{{NoSampleHint}}}"))

        // For projects with no samples yet, offer to create one automatically.
        boolean noSamples = sampleTags.length == 1 // only the "no sample" option
        if (noSamples) {
            params.createSample = [value: true,
                DISPLAY_NAME: "Create sample automatically", TYPE: Boolean,
                CAN_BE_NULL: true]
        }

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
        // Selected sample ("" or null = no sample). Stored in file_info.entityID.
        def sampleIdVal = params.getValue("sampleId")?.toString()
        Long sampleId = (sampleIdVal != null && !sampleIdVal.isBlank()) ? Long.valueOf(sampleIdVal) : null
        // "Create sample automatically" — only shown for projects with no samples.
        def createSampleVal = params.getValue("createSample")
        boolean createSample = createSampleVal == Boolean.TRUE || "true".equalsIgnoreCase(createSampleVal?.toString())

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

        // "Create sample automatically": for a project with no samples, create
        // sample1/sample2/... and assign it to the imported files.
        if (createSample && sampleId == null) {
            try {
                sampleId = nextAutoSampleId(projectId)
            } catch (Exception e) {
                setResult(OperationResult.error("Failed to create a sample: " + e.message))
                return
            }
        }

        DataCollection samples = DataCollectionUtils.createSubCollection(projectPath.getChildPath("samples"), true)

        Object[] files = params.$files
        List<String> errors = []
        int imported = 0
        // Collect paths of files actually imported (newly created on disk)
        List<String> importedPaths = []
        // Collect paths of directories created by archive unpacking, registered
        // in file_info with a 'folder' type
        List<String> importedFolders = []

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
                    RepositoryManager.importArchiveFile(tempFile, samples, importedPaths, importedFolders)
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
        // importedPaths are repo-root-relative; resolve against the absolute repo root.
        for (String pathStr in importedPaths) {
            def existing = db.oneLong(
                    "SELECT ID FROM file_info WHERE path = ? AND project = ?",
                    pathStr, projectId)
            if (existing != null) continue

            File absFile = new File(repoPath, pathStr)
            def typeID = FileTypeUtils.getOrCreateTypeId(db, absFile)
            database.file_info << [
                fileName: new File(pathStr).getName(),
                fileType: typeID,
                path: pathStr,
                size: absFile.length(),
                project: projectId,
                entity: "samples",
                entityID: sampleId,
                comment: comment
            ]
        }

        // Register directories created by archive unpacking with a 'folder' file type
        if (!importedFolders.isEmpty()) {
            def folderTypeId = FileTypeUtils.getOrCreateFolderTypeId(db)
            for (String folderPath in importedFolders) {
                def existing = db.oneLong(
                        "SELECT ID FROM file_info WHERE path = ? AND project = ?",
                        folderPath, projectId)
                if (existing != null) continue

                    database.file_info << [
                        fileName: new File(folderPath).getName(),
                        fileType: folderTypeId,
                        path: folderPath,
                        size: 0,
                        project: projectId,
                        entity: "samples",
                        entityID: sampleId,
                        comment: comment
                    ]
            }
        }

        if (errors.isEmpty()) {
            setResult(OperationResult.finished("Imported ${imported} file(s)"))
        } else {
            setResult(OperationResult.finished("Some files failed: " + String.join("\n", errors)))
        }
    }

    /**
     * Creates a new sample titled sample1, sample2, ... (the next free
     * sequential number for the project) and returns its ID.
     */
    private Long nextAutoSampleId(long projectId) {
        int next = 1
        def rows = db.listWithParams("SELECT title FROM samples WHERE project = ?", projectId)
        for (def r : rows) {
            def t = r.$title?.toString()
            if (t == null) continue
            def m = (t =~ /sample(\d+)$/)
            if (m.find()) {
                int n = m.group(1).toInteger()
                if (n >= next) next = n + 1
            }
        }
        String title = "sample" + next
        // database.<entity> << [...] returns the generated autoincrement ID.
        return (Long) (database.samples << [project: projectId, title: title])
    }
}
