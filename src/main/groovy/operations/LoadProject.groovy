package operations

import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.util.logging.Level

import ru.biosoft.lims.parsers.IlluminaCSVParser
import ru.biosoft.lims.parsers.YamlParser
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.access.DataCollectionUtils

import javax.inject.Inject
import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.IOUtils

import ru.biosoft.access.core.DataElementPath
import ru.biosoft.access.core.DataCollection
import ru.biosoft.access.core.DataElement
import ru.biosoft.access.core.DataElementImporter
import ru.biosoft.access.file.GenericFileDataCollection
import ru.biosoft.access.file.FileTypeRegistry
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
    private static String ILLUMINA_SAMPLE_SHEET = "Illumina"
    private static String YAML_SAMPLE_SHEET = "Нанофор СПС"

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
                ILLUMINA_SAMPLE_SHEET,
                YAML_SAMPLE_SHEET,
                "Other"
            ] as String[] ]

        DynamicProperty prop = new DynamicProperty("sequencesFile", "Sequences (fastg.gz or fastq files)", java.io.File.class);
        prop.setAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST, true )
        params.add(prop)
        dpsHelper.addLabelRaw(params, "Single or multiple files with *.fastq or *.fastq.gz (.tar, .tar.gz, zip archive with multiple fasta is allowed)")

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
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()
        def projectName = params.$projectName
        if(isProjectExists(projectName)) {
            setResult(OperationResult.error("The project name you have entered already exists."))
            return
        }

        String repoPath = repo.getRepositoryPath();
        if(repoPath == null) {
            setResult(OperationResult.error("Repository is not available"))
            return
        }

        DataElementPath parentPath = DataElementPath.create(repoPath);
        DataCollection proj = null;
        try {
            proj = DataCollectionUtils.createSubCollection(parentPath.getChildPath(projectName ));
        }
        catch (Exception e) {
            setResult(OperationResult.error("Error creating project: " + e.getMessage()));
            return
        }

        List<String> errors = new ArrayList<>()
        def description = params.$description
        def projectId = database.projects << [name: projectName, description: description]

        //TODO: Check if it is file name only or with path. Documentation says some browsers return full path.
        FileItem sampleSheetItem = getFileItem( params.$sampleSheetFile )
        def fileName = sampleSheetItem.getName();

        File sampleSheetFile = repo.getChildFile(proj, fileName );

        //copy sampleSheetFile to file
        InputStream source = sampleSheetItem.getInputStream(); FileOutputStream destination = new FileOutputStream(sampleSheetFile)
        try {
            IOUtils.copy(source, destination);
        }
        catch( Exception e ) {
            errors.add("Error copy sampleSheet file: " + e.getMessage() )
            sampleSheetFile = null
        }
        finally {
            source.close();
            destination.close();
        }

        DataCollection samples = DataCollectionUtils.createSubCollection(proj.getCompletePath().getChildPath("samples" ));

        Map<String, File> namesNoExt = [:]

        Object[] files = params.$sequencesFile;
        FileItem seqfile = null;
        if(files.length == 1) {
            //Single file, it can be simple fasta (gzipped or not) or archive
            //Try to import as archive first, if...
            seqfile = getFileItem( files[0] )
            try {
                File tempDir = TempFiles.getTempDirectory();
                File seqfileTmp = new File(tempDir, seqfile.name)
                source = seqfile.getInputStream();
                destination = new FileOutputStream(seqfileTmp)
                try {
                    IOUtils.copy(source, destination);
                }
                finally {
                    source.close();
                    destination.close();
                }
                if(ArchiveFactory.isComplexArchive(seqfileTmp)) {
                    repo.importArchiveFile(seqfileTmp, samples)
                }
                else {
                    //single fasta, copy as is
                    ApplicationUtils.copyFile(repo.getChildFile(samples, seqfile.name ), seqfileTmp )
                }
            }catch(Exception e) {
                errors.add("Error processing sequence file(s): " + e.getMessage())
            }
        }
        else
            for(int i = 0; i < files.length; i++) {
                seqfile = getFileItem( files[i] )
                try {
                    File seqfileTmp = repo.getChildFile(samples, seqfile.name );
                    source = seqfile.getInputStream();
                    destination = new FileOutputStream(seqfileTmp)
                    try {
                        IOUtils.copy(source, destination);
                    }
                    finally {
                        source.close();
                        destination.close();
                    }
                }catch(Exception e) {
                    errors.add("Error processing sequence file(s): " + e.getMessage())
                }
            }
        for (String name: samples.getNameList()) {
            File deFile = repo.getChildFile(samples, name);
            namesNoExt[deFile.getName().replaceFirst(~/\..+$/, '')] = deFile;
        }

        //Sample sheet file is null or incorrect, do not add run info to database
        if(sampleSheetFile == null) {
            errors.add("Sample sheet file is not valid, no run information is available")
            setResult(OperationResult.finished(String.join("\n", errors)))
            return
        }

        String sampleSheetFormat = params.$sampleSheetFormat
        String runName = sampleSheetFormat + " run";
        Map<String, Object> runInfo;

        if(sampleSheetFormat.equals(ILLUMINA_SAMPLE_SHEET )) {
            try {
                runInfo = IlluminaCSVParser.parseFile(sampleSheetFile)
            }
            catch(Exception e) {
                errors.add("Error parsing sample sheet file: " + e.getMessage())
            }
        }
        else if(sampleSheetFormat.equals(YAML_SAMPLE_SHEET)) {
            try {
                runInfo = YamlParser.parseFile(sampleSheetFile)
                runName = runInfo.containsKey("Sequence") ? runInfo.get("Sequence").getOrDefault("Name", YAML_SAMPLE_SHEET + " run") : YAML_SAMPLE_SHEET + " run"
            }
            catch(Exception e) {
                errors.add("Error parsing sample sheet file: " + e.getMessage())
            }
        }
        else {
            errors.add("Unknown sample sheet format, file " + sampleSheetFile.getName() + " can not be parsed")
        }

        def fileData = IOUtils.toString(sampleSheetItem.getInputStream(), StandardCharsets.UTF_8);
        def runId = database.runs << [project_id: projectId, name: runName, status:"completed", data:fileData]

        String sampleFileName = sampleSheetFile.getName();
        String sampleFileExt = sampleFileName.replaceFirst(~/^([^\.])+/, '')
        long fileType = getFileType(sampleSheetFile, sampleFileExt)
        def fileId = database.file_info << [fileName: sampleFileName,  fileType: fileType,
            path: proj.getCompletePath().getChildPath(sampleFileName).toString(), size: sampleSheetFile.length(), project: projectId,
            entity: "run", entityID: runId]
        //samples
        //first row - column names
        List<List<String>> samplesData = sampleSheetFormat.equals(ILLUMINA_SAMPLE_SHEET ) ? runInfo.get("Data" ) : runInfo.get("Samples");
        if(samplesData!= null && !samplesData.isEmpty()) {
            Map<String, Integer> nameToIndex = new HashMap<>();
            List<String> names = samplesData.get(0);
            for(int i = 0; i < names.size(); i++) {
                nameToIndex.put(names.get(i ), i );
            }
            def runSampleFields = [:]
            runSampleFields["plate"] = "Sample_ID"
            runSampleFields["well"] = "Sample_Well"
            runSampleFields["index_plate_well"] = "Index_Plate_Well"
            runSampleFields["I7_index_id"] = "I7_Index_ID"
            runSampleFields["index1"] = "index"
            runSampleFields["I5_index_id"] = "I5_Index_ID"
            runSampleFields["index2"] = "index2"
            for(int i = 1; i < samplesData.size(); i++) {
                List<String> sampleValues = samplesData.get(i);
                String sampleName = getOrDefault(sampleValues, nameToIndex, "Sample_ID" );
                long sampleTypeId  = Integer.valueOf(params.$sampleType)
                def sampleId = database.samples << [project: projectId, typeID: sampleTypeId, title: sampleName, description: getOrDefault(sampleValues, nameToIndex, "Description" ) ]


                def runSample = [:].withDefault{ key ->
                    return ""
                }
                runSampleFields.each{ k, v ->
                    runSample[k] = getOrDefault(sampleValues, nameToIndex, v )
                }
                runSample["run_id"] = runId
                runSample["sample_id"] = sampleId
                def runSampleId = database.run_samples << runSample
                if(params.$readsType.equals("single" )) {
                    if(namesNoExt.containsKey(sampleName)) {
                        File fname = namesNoExt.get(sampleName);
                        long fileTypeId = getFileType(fname, fname.getName().replace(sampleName, ""))
                        fileId = database.file_info << [fileName: fname.name,  fileType: fileTypeId, path: samples.getCompletePath().getChildPath(fname.name).toString(), size: fname.length(), project: projectId, entity: "sample", entityID: sampleId]
                    }
                }
                else if(params.$readsType.equals("paired" )) {
                    def readName = sampleName + "_r1";
                    if(namesNoExt.containsKey(readName)) {
                        File fname = namesNoExt.get(readName);
                        long fileTypeId = getFileType(fname, fname.getName().replace(sampleName, ""))
                        fileId = database.file_info << [fileName: fname.name,  fileType: fileTypeId, path: samples.getCompletePath().getChildPath(fname.name).toString(), size: fname.length(), project: projectId, entity: "sample", entityID: sampleId]
                    }
                    readName = sampleName + "_r2";
                    if(namesNoExt.containsKey(readName)) {
                        File fname = namesNoExt.get(readName);
                        long fileTypeId = getFileType(fname, fname.getName().replace(sampleName, ""))
                        fileId = database.file_info << [fileName: fname.name,  fileType: fileTypeId, path: samples.getCompletePath().getChildPath(fname.name).toString(), size: fname.length(), project: projectId, entity: "sample", entityID: sampleId]
                    }
                }
            }
        }
        else {
            errors.add("Sample data is empty or not valid, no samples information was loaded" )
        }

        //if(errors.isEmpty())
        setResult(OperationResult.finished())
        //        else {
        //            errors.add(0, "<p class='invalid-feedback'>" )
        //            errors.add("</p>" )
        //            OperationResult.finished(String.join("\n", errors))
        //        }
    }

    def String getOrDefault(List<String> values, Map<String, Integer> keyToIndex, String key) {
        if(keyToIndex.containsKey(key )) {
            int index = keyToIndex.get(key );
            if(values.size() <= index)
                return "";
            return values.get(index);
        }
        else
            return "";
    }

    def boolean isProjectExists(String projectName) {
        if(projectName == null)
            return false;
        def projID = db.oneLong( "SELECT id FROM projects prj WHERE name = ?", projectName)
        return ( projID != null );
    }

    def long getFileType(File file, String fileExt) {

        def typeID = db.oneLong( "SELECT id FROM file_types prj WHERE suffix = ?", fileExt)
        if(typeID != null)
            return Long.valueOf(typeID)
        def genericType = ".binary"
        if(FileTypeRegistry.isTextFile(file)) {
            genericType = ".text"
        }
        typeID = db.oneLong( "SELECT id FROM file_types prj WHERE suffix = ?", genericType)
        if(typeID != null)
            return typeID
        else {
            def descr = genericType.equals(".text" )?"Generic text file": "Generic binary file"
            typeID = database.file_types << [suffix: genericType, description: descr]
            return typeID
        }
    }

    def long getSampleType(String name) {
        def typeID = db.oneLong( "SELECT id FROM sample_templates WHERE title = ?", name)
        if(typeID != null)
            return Long.valueOf(typeID)
        else
            return 0
    }
}
