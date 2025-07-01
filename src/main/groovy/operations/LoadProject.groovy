package operations

import java.nio.channels.FileChannel
import java.util.logging.Level

import ru.biosoft.util.ApplicationUtils
import ru.biosoft.util.DataCollectionUtils

import javax.inject.Inject
import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.IOUtils

import ru.biosoft.access.core.DataElementPath
import ru.biosoft.access.core.DataCollection
import ru.biosoft.access.core.DataElement
import ru.biosoft.access.core.DataElementImporter
import ru.biosoft.access.file.GenericFileDataCollection
import ru.biosoft.jobcontrol.FunctionJobControl
import ru.biosoft.jobcontrol.JobControl
import ru.biosoft.jobcontrol.SubFunctionJobControl
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.util.archive.ArchiveFile
import ru.biosoft.util.archive.ComplexArchiveFile
import ru.biosoft.util.TempFiles
import ru.biosoft.util.archive.ArchiveEntry
import ru.biosoft.util.archive.ArchiveFactory
import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.server.model.Base64File
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.be5.operation.OperationResult

import one.util.streamex.StreamEx
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.BeanInfoConstants



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
        params.sampleType = [value: null,
            DISPLAY_NAME: "Sample type", TYPE: String,
            TAG_LIST_ATTR: queries.getTagsFromSelectionView("sample_templates") ]

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

        DynamicProperty prop = new DynamicProperty("sequencesFile", "Sequences (fastg.gz or fastq files)", java.io.File.class);
        prop.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true )
        params.add(prop)
        dpsHelper.addLabelRaw(params, "Single or multiple files with *.fastq or *.fastq.gz, as well as *.tar or *.zip archives with multiple fasta are allowed (do not use *.tar.gz complex archive).")
        //TODO: !!!! архив архивов или отдельные файлы?
        //Архив fastq.gzip - архив с последовательностями

        //        params.sequencesFile = [ value: null, TYPE: java.io.File,
        //            DISPLAY_NAME: "Sequences (.tar.gz archive)",
        //            MULTIPLE_SELECTION_LIST: "true" ]

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
        def projectId = database.projects << [name: projectName, description: description]

        //insertData("projects", ["name", "description"], projectName, description)

        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        String repoPath = repo.getRepositoryPath();
        if(repoPath != null) {
            DataElementPath parentPath = DataElementPath.create(repoPath);
            DataCollection proj = null;
            try {
                proj = DataCollectionUtils.createSubCollection(parentPath.getChildPath(projectName ));
            }
            catch (Exception e) {
                throw new NullPointerException("Error creating sub collection: " + e.getMessage());
            }

            //TODO: Check if it is file name only or with path. Documentation says some browsers return full path.
            FileItem sampleSheetFile = getFileItem( params.$sampleSheetFile )
            def fileName = sampleSheetFile.getName();

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
                setResult(OperationResult.error("Can not write Sample Sheet to project repository"))
            }

            DataCollection samples = DataCollectionUtils.createSubCollection(proj.getCompletePath().getChildPath("samples" ));

            Object[] files = params.$sequencesFile;
            if(files.length == 1) {
                //Single file, it can be simple fasta (gzipped or not) or archive
                //Try to import as archive first, if...
                FileItem seqfile = getFileItem( files[0] )
                try {
                    File tempDir = TempFiles.getTempDirectory();
                    File seqfileTmp = new File(tempDir, seqfile.getName())
                    InputStream source = seqfile.getInputStream();
                    FileOutputStream destination = new FileOutputStream(seqfileTmp)
                    try {
                        IOUtils.copy(source, destination);
                    }
                    finally {
                        source.close();
                        destination.close();
                    }
                    int numInArchive = ArchiveFactory.getCount(seqfileTmp)
                    if(numInArchive <=1) {
                        //single fasta, copy as is
                        ApplicationUtils.copyFile(repo.getChildFile(samples, seqfile.getName() ), seqfileTmp )
                    }
                    else {
                        repo.importArchiveFile(seqfileTmp, samples)
                    }
                }catch(Exception e) {
                    setResult(OperationResult.error("Error processing Sequence file: " + e.getMessage()))
                }
            }
            else
                for(int i = 0; i < files.length; i++) {
                    FileItem seqfile = getFileItem( files[i] )
                    try {
                        File tempDir = TempFiles.getTempDirectory();
                        //File seqfileTmp = new File(tempDir, seqfile.getName())
                        File seqfileTmp = repo.getChildFile(samples, seqfile.getName() );
                        InputStream source = seqfile.getInputStream();
                        FileOutputStream destination = new FileOutputStream(seqfileTmp)
                        try {
                            IOUtils.copy(source, destination);
                        }
                        finally {
                            source.close();
                            destination.close();
                        }
                    }catch(Exception e) {
                        setResult(OperationResult.error("Error processing Sequence file: " + e.getMessage()))
                    }
                }
        }
        else {
            setResult(OperationResult.error("Repository is not available"))
            return
        }

        //TODO: parse sample sheet
        //TODO: fill tables with info
        setResult(OperationResult.finished())
    }

    def boolean isProjectExists(String projectName) {
        if(projectName == null)
            return false;
        def projID = db.oneLong( "SELECT id FROM projects prj WHERE name = ?", projectName)
        return ( projID != null );
    }
}
