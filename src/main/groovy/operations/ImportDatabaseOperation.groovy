package operations

import java.nio.file.Files
import java.nio.file.Path
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

import one.util.streamex.StreamEx

import com.developmentontheedge.beans.BeanInfoConstants

import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.IOUtils
import ru.biosoft.access.core.DataCollection
import ru.biosoft.access.core.DataElementPath
import ru.biosoft.access.file.FileBasedCollection
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.util.TempFiles
import ru.biosoft.util.archive.ArchiveFactory
import ru.biosoft.util.archive.ArchiveFile
import ru.biosoft.util.archive.ArchiveEntry
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.util.RepositoryFileUtils
import ru.biosoft.lims.util.FileTypeUtils

import ru.biosoft.access.DataCollectionUtils
import ru.biosoft.access.LocalRepository

public class ImportDatabaseOperation extends GOperationSupport {

    private static final Logger log = Logger.getLogger(ImportDatabaseOperation.class.getName())

    @Inject
    private RepositoryManager repo

    Map<String, Object> presets

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        presets = presetValues

        Long workflowId = null
        for ( int i=0 ; i < context.records.length ; ++i ) {
            workflowId = context.records[i]
        }
        params.workflowId = [value: workflowId,
            DISPLAY_NAME: "Workflow", TYPE: Long,
            HIDDEN: true, CAN_BE_NULL: false]

        def prjId = context.getParams().getOrDefault("___prjID", presetValues.get("___prjID"))
        if (prjId != null) {
            prjId = Long.valueOf(prjId.toString())
        }

        // Display the project name as a read-only label
        def workflowName = workflowId ? db.oneString("SELECT title FROM workflow_info WHERE ID = ?", workflowId) : ""
        params.workflowName = [value: workflowName,
            DISPLAY_NAME: "Importing database for workflow",
            TYPE: String,
            HIDDEN: false,
            READ_ONLY: true,
            CAN_BE_NULL: true]

        params.files = [TYPE: java.io.File,
            DISPLAY_NAME: "Files to import",
            CAN_BE_NULL: false]

        params.comment = [value: "", TYPE: String,
            DISPLAY_NAME: "Comment",
            CAN_BE_NULL: true]

        params = DpsUtils.setValues(params, presetValues)

        return params
    }

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()
        Long workflowId = params.$workflowId
        if (workflowId != null) {
            workflowId = Long.valueOf(workflowId.toString())
        }
        String comment = params.$comment?.toString() ?: ""

        if (workflowId == null) {
            setResult(OperationResult.error("Worflow is not specified"))
            return
        }

        Path repositoryPath = repo.getDatabasesPath()
        if (repositoryPath == null) {
            setResult(OperationResult.error("Databases repository is not available"))
            return
        }

        FileItem fileItem = getFileItem(params.getValue("files"))
        if (fileItem == null || fileItem.isFormField()) {
            setResult(OperationResult.error("No file specified"))
            return
        }

        String fileName = fileItem.name
        def dbName = RepositoryFileUtils.stripArchiveExtensions(fileName )

        File tempFile = new File(TempFiles.getTempDirectory(), fileName)
        try {
            fileItem.inputStream.withCloseable { ins ->
                tempFile.withOutputStream { outs ->
                    IOUtils.copy(ins, outs)
                }
            }
            ArchiveFile archiveFile = ArchiveFactory.getArchiveFile(tempFile)
            if (archiveFile != null) {
                //TODO: pending: add checkbox to register already existed database to selected workflow
                String dbDirName = importArchiveFile(archiveFile, repositoryPath, fileName)
                DataElementPath dbPath = DataElementPath.create("databases").getChildPath(dbDirName)
                String dbPathStr = dbPath.toString()

                //Register database to workflow_info
                def existing = db.oneLong( "SELECT ID FROM file_info WHERE path = ? AND entity='workflow_info' AND entityID=?",
                dbPathStr, workflowId)
                if (existing == null) {
                    def folderTypeId = FileTypeUtils.getOrCreateFolderTypeId(db)
                    database.file_info << [
                        fileName: dbDirName,
                        fileType: folderTypeId,
                        path: dbPathStr,
                        size: fileItem.getSize(),
                        project: null,
                        entity: "workflow_info",
                        entityID: workflowId,
                        comment: comment
                    ]
                }
                ((LocalRepository)dbPath.getParentCollection()).updateRepository()
                setResult(OperationResult.finished("Imported database ${dbDirName}"))
            }
            else {
                //Error: we use only archives to import as database
                setResult(OperationResult.error("File ${fileName} is not an archive; only archives can be imported as a database"))
                return
            }
        } catch (Exception e) {
            //log.error("Error importing ${fileName}: ${e.message}")
            setResult(OperationResult.error("Error importing ${fileName}: ${e.message}"))
            return
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Unpacks an archive into the tmp directory, preserving the archive's
     * internal folder structure. 
     * Decide the destination folder:
     *  - If the archive unpacks into a single top-level folder, use that
     *  folder's name as the database directory (it becomes a child of 'databases').
     *  - Otherwise (several top-level items), create a preliminary folder
     *  named after the archive (without extension) and unpack into it.
     *  Copy all files from tmp to destination folder.
     */
    public static String importArchiveFile(ArchiveFile archiveFile, Path repository, String archiveName) throws Exception {
        if( archiveFile == null )
        throw new Exception( "Specified file is not an archive" );
        int i = 0;
        ArchiveEntry entry;
        int numImported = 0;
        List<String> importErrors = new ArrayList<>();
        File dest = TempFiles.dir("zipDBImport_" +  archiveName);

        Map<String, Boolean> topLevel = new LinkedHashMap<>()  // name -> isDirectory
        while ( (entry = archiveFile.getNextEntry()) != null ) {
            i++;
            // Split the archive entry path; drop "." and ".." segments.
            String[] pathFields = StreamEx.of( entry.getName().split( "[\\\\\\/]+" ) )
            .without( "." ).without( ".." ).remove( String::isEmpty ).toArray( String[]::new );
            if( pathFields.length == 0 )
            continue;

            String entryName = pathFields[pathFields.length - 1];
            // Rebuild the nested destination path under dest.
            File entryDest = dest;
            for ( int j = 0; j < pathFields.length; j++ ) {
                entryDest = new File( entryDest, pathFields[j] );
            }
            if( entry.isDirectory() ) {
                if( !entryDest.exists() && !entryDest.mkdirs() )
                importErrors.add( "Could not create folder " + entryDest );
                continue;
            }
            // Ensure the parent folder exists, then copy the file.
            if( !entryDest.getParentFile().exists() && !entryDest.getParentFile().mkdirs() )
            importErrors.add( "Could not create folder " + entryDest.getParentFile() );
            InputStream is = entry.getInputStream();
            File tmpFile = TempFiles.file( "zipImport" + i, is );
            try {
                ApplicationUtils.copyFile( entryDest, tmpFile, null );
                numImported++;
            }
            catch (Exception e) {
                importErrors.add( e.getMessage() );
            }
            finally {
                tmpFile.delete();
            }
        }
        if( numImported == 0 ) {
            throw new Exception( "No importable or valid files were found in archive. Please check your input file." );
        }
        if( !importErrors.isEmpty() ) {
            throw new Exception( "Error importing archived data: \n" + String.join( "\n", importErrors ) );
        }
        //It archive had one top-level folder, it will be database itself.
        //Any collection may be packed with configs this way.
        //If archive had several top-level items packed in one file, create a new folder for database with name as archive name
        //In this case it will be GenericFileCollection
        File source = dest;
        String dbName = null;
        File[] tops = dest.listFiles();
        if(tops.length == 1 && tops[0].isDirectory()) {
            source = tops[0];
            dbName = source.getName();
        }
        else {
            dbName =  RepositoryFileUtils.stripArchiveExtensions(archiveName);
        }
        if(DataElementPath.create("databases" ).getChildPath(dbName ).exists()) {
            //Database already exist, remove all unpacked and return error
            dest.delete();
            throw new Exception("Database " + dbName + " already exists.");
        }
        Path destPath = repository.resolve(dbName );
        Path sourcePath = source.toPath();
        RepositoryFileUtils.copyDirOverwrite(sourcePath, destPath );
        dest.delete();
        return dbName;
    }
}
