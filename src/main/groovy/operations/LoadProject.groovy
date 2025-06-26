package operations

import java.nio.channels.FileChannel

import javax.inject.Inject
import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.IOUtils
import ru.biosoft.access.core.DataCollection
import ru.biosoft.access.core.DataElementPath
import ru.biosoft.access.file.GenericFileDataCollection
import ru.biosoft.lims.repository.RepositoryManager

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.server.model.Base64File
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.be5.operation.OperationResult

import javassist.bytecode.stackmap.BasicBlock.Catch

import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport



public class LoadProject extends GOperationSupport {
    Map<String, Object> presets

    @Inject
    private RepositoryManager repo;

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        presets = presetValues

        params.projectName = [value: presetValues.get("projectName" ),
            DISPLAY_NAME: "Project name", TYPE: String]

        params.description = [value: "",
            DISPLAY_NAME: "Description", TYPE: String]

        // Тип образцов (из справочника Sample types) может быть пустым, если образцы разных типов
        //TODO: Ask Fedor and Zha: where to get dictionaries
        params.sampleType = [value: null,
            DISPLAY_NAME: "Sample type", TYPE: String,
            TAG_LIST_ATTR: [
                "Sample type 1",
                "Sample type 2",
                "unknown"
            ] as String[] ]

        //Тип прочтения (парные или нет)
        params.readsType = [value: null,
            DISPLAY_NAME: "Reads type", TYPE: String,
            TAG_LIST_ATTR: ["paired", "single", "unknown"] as String[] ]

        //Sample sheet - файл
        params.sampleSheetFile = [ TYPE: java.io.File,
            DISPLAY_NAME: "Sample sheet file" ]

        //Sample sheet формат (Illumina/Нанофор СПС)
        params.sampleSheetFormat = [ value: "Illumina",
            TYPE: String,
            DISPLAY_NAME: "Sample sheet format",
            TAG_LIST_ATTR: [
                "Illumina",
                "Нанофор СПС",
                "Other"
            ] as String[] ]

        //TODO: !!!! архив архивов или отдельные файлы?
        //Архив fastq.gzip - архив с последовательностями
        params.sequencesFile = [ value: null, TYPE: java.io.File,
            DISPLAY_NAME: "Sequences (.gzip archive)" ]

        params = DpsUtils.setValues(params, presetValues)

        def projectName = params.getValue("projectName")
        if(isProjectExists(projectName)) {
            validator.setError( params.projectName, "The project name you have entered already exists.\nPlease try again." )
            return params
        }

        return params
    }

    /*
     * создает:
     + запись в таблице project
     +директорию в папке projects 
     (см. lims-test-hemotology\projects\test-hematology как образец)
     +копирует туда SampleSheet
     создает папку samples и копирует туда образцы в виде fastq.gzip
     разбирает SampleSheet и для каждого образца создает запись в таблицах
     samples
     run_samples
     file_info
     Для парных ридов - 2 файла на образец.
     */

    @Override
    public void invoke(Object parameters) throws Exception {
        def projectName = params.getValue("projectName")
        if(isProjectExists(projectName)) {
            setResult(OperationResult.error("The project name you have entered already exists."))
        }
        def description = params.getValue("description")
        insertData("projects", ["name", "description"], projectName, description)

        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        String repoPath = repo.getRepositoryPath();
        if(repoPath != null) {
            DataElementPath parentPath = DataElementPath.create(repoPath);
            DataCollection proj = null;
            try {
                proj = repo.createSubCollection(parentPath.getChildPath(projectName ));
            }
            catch (Exception e) {
                throw new NullPointerException("Error creating sub collection: " + e.getMessage());
            }

            //TODO: Check if it is file name only or with path. Documentation says some browsers return full path.
            FileItem sampleSheetFile = getFileItem( params.$sampleSheetFile )
            def fileName = sampleSheetFile.getName();

            //TODO: Ask Fedor: we use only GenericFileDataCollecion or use general approach with all Protected/Primary collection processing steps? Now partially copied to RepositoryManager
            def file = repo.getChildFile(proj, fileName );

            //copy sampleSheetFile to file
            if(file != null) {
                InputStream source = sampleSheetFile.getInputStream(); FileOutputStream destination = new FileOutputStream(file)
                try {
                    IOUtils.copy(source, destination);
                }
                catch( Exception e ) {
                    throw new NullPointerException("Error copy sampleSheet file: " + e.getMessage());
                }
                finally {
                    source.close();
                    destination.close();
                }
            }
            else {
                //TODO:
            }

            DataCollection samples = repo.createSubCollection(proj.getCompletePath().getChildPath("samples" ));

            FileItem seqfile = getFileItem( params.$sequencesFile )
            //TODO: Ask Fedor: we always have only one gzip archive?  If many samplles of fasta.gz, they will be compressed to one single .gzip?

            //TODO: unpack sample arhive
            //TODO: copy unpacked to 'samples' collection
            //                ArchiveFile archiveFile = ArchiveFactory.getArchiveFile(seqfile);
            //                if(archiveFile != null) {
            //                    archiveFile.close();
            //                    return ACCEPT_HIGH_PRIORITY;
            //                }
        }
        else {
            setResult(OperationResult.error("Repository is not available"))
        }

        //TODO: parse sample sheet
        //TODO: fill tables with info
    }

    //TODO: Ask Fedor and Zha: Who inserted, who modified not set by default
    def insertData(String tableName, List<String> columns, Object... params) {
        def insertQuery = "INSERT INTO ${tableName} (${columns.join(", ")}) VALUES (${columns.collect { column -> "?" }.join(", ")})"
        db.insertRaw( insertQuery, params )
    }

    def boolean isProjectExists(String projectName) {
        if(projectName == null)
            return false;
        def projID = db.oneLong( "SELECT id FROM projects prj WHERE name = ?", projectName)
        return ( projID != null );
    }
}
