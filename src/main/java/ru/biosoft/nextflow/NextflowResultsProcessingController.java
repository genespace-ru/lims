package ru.biosoft.nextflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.io.FilenameUtils;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.file.FileTypeRegistry;
import ru.biosoft.access.file.GenericFileDataCollection;
import ru.biosoft.lims.repository.RepositoryManager;
import ru.biosoft.util.ApplicationUtils;

public class NextflowResultsProcessingController extends NextflowController
{

    private static final Logger log = Logger.getLogger(NextflowResultsProcessingController.class.getName() );
    
    @Inject 
    protected DbService db;
    
    @Inject
    private RepositoryManager repo;

    @Override
    protected String process(Request req, Response res, JsonObject body) throws Exception
    {
        String uri = req.getRequestUri();
        String[] tokens = uri.split("/"); 

        String method = req.getRawRequest().getMethod();
        
        if( "POST".equals( method ) && "parse".equals( tokens[2] ) )
        {
            if( "multiqc".equals( tokens[3] ) )
                return parseMultiQCResults( req, body );
            if( "results".equals( tokens[3] ) )
                return processWorkflowResults( req, body );
        }

        throw unknownRequest(req);
    }

    private String parseMultiQCResults(Request req, JsonObject body)
    {
        Integer workflowId = body.getInt( "workflowId" );
        Integer projectId = body.getInt( "prjId" );
        String repoPath = repo.getRepositoryPath();

        String projectName = db.getString( "SELECT name FROM projects WHERE ID=?", projectId );
        DataElementPath projectPath = DataElementPath.create( repoPath ).getChildPath( projectName );
        Integer qcRunId = null;

        String resFile = body.getString( "results" );
        if( resFile != null )
        {
            File qcResults = new File( resFile );
            try
            {
                String res = ApplicationUtils.readAsString( qcResults );
                String[] resLines = res.split( "\n" );
                if( resLines.length > 0 )
                {

                    String[] columns = resLines[0].split( "\\s+" );
                    String sql = "INSERT INTO qc_runs(project, sample, file_info, workflow, qc_info) VALUES(?, ?, ?, ?, ?::jsonb)";
                    for ( int i = 1; i < resLines.length; i++ )
                    {
                        String[] values = resLines[i].split( "\\s+" );
                        JsonObjectBuilder job = Json.createObjectBuilder();
                        String fileName = null;
                        String sample = null;
                        int maxColNum = Math.min( columns.length, values.length );
                        for ( int j = 0; j < maxColNum; j++ )
                        {
                            if( columns[j].equalsIgnoreCase( "Filename" ) )
                                fileName = values[j];
                            else if( columns[j].equalsIgnoreCase( "sample" ) )
                                sample = values[j];
                            else
                                job.add( columns[j], values[j] );
                        }
                        JsonObject params = job.build();
                        Long sampleId = null;
                        if( sample != null )
                        {
                            sampleId = db.oneLong( "SELECT ID FROM samples WHERE title=? AND project=? ", sample, projectId );
                            if( sampleId == null )
                            {
                                //try to remove reads suffix
                                sample = sample.substring( 0, sample.lastIndexOf( "_" ) );
                                sampleId = db.oneLong( "SELECT ID FROM samples WHERE title=? AND project=? ", sample, projectId );
                            }
                        }
                        Long fileInfo = fileName != null ? db.oneLong( "SELECT ID FROM file_info WHERE filename=? AND project=? ", fileName, projectId ) : null;
                        qcRunId = db.updateRaw( sql, projectId, sampleId, fileInfo, workflowId, params.toString() );
                    }
                }

            }
            catch (IOException e)
            {
            }
        }
        String reportFileStr = body.getString( "report" );
        if( reportFileStr != null )
        {
            File reportFile = new File( reportFileStr );
            File resultsFolder = ((GenericFileDataCollection) projectPath.getDataCollection()).getFile( "results" );
            Path pathRelative = resultsFolder.toPath().relativize( reportFile.toPath() );

            DataElementPath results = projectPath.getChildPath( "results" );
            String[] parts = pathRelative.toString().split( Pattern.quote( File.separator ) );
            DataElementPath reportPath = results.getChildPath( parts );

            Long fileTypeId = db.oneLong( "SELECT ID FROM file_types WHERE suffix=?", reportFile.getName() );
            if( reportPath.exists() )
            {
                db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)", reportFile.getName(), fileTypeId,
                        reportPath.toString(), reportFile.length(), projectId, "workflow_runs", workflowId );
            }

        }

        return "{ \"result\":\"ok\"}";

    }

    public String processWorkflowResults(Request req, JsonObject body)
    {
        Integer workflowRunId = body.getInt( "workflowId" );
        QRec wReq = db.recordWithParams( "SELECT pr.name, comment, pr.id as projectId FROM workflow_runs wr JOIN projects pr ON pr.id=wr.project WHERE wr.id=?", workflowRunId );
        if( wReq != null )
        {
            //Parse outputs.json and register files, if no outputs, parse folders and register files
            String projectName = wReq.getString( "name" );
            String runFolderPath = wReq.getString( "comment" );
            Long projectId = wReq.getLong( "projectId" );
            if( projectName != null && runFolderPath != null )
            {
                String repoPath = repo.getRepositoryPath();
                DataElementPath resultsPath = DataElementPath.create( repoPath ).getChildPath( projectName ).getChildPath( "results" ).getChildPath( runFolderPath );
                GenericFileDataCollection folder = resultsPath.getDataElement( GenericFileDataCollection.class );

                File outputsPath = folder.getFile( "outputs.json" );
                //Here we need outputs.json containing absolute paths in projects/ProjectName/results/workflow_run folder
                //This folder is set as base publishDir in nextflow generation process 
                if( outputsPath.exists() )
                {
                    File resultsFile = ((GenericFileDataCollection) folder.getOrigin()).getFile( folder.getName() );
                    Path resultsPath2 = resultsFile.toPath();
                    try( JsonReader jsonReader = Json.createReader( new java.io.FileReader( outputsPath ) ) )
                    {
                        JsonObject outputs = jsonReader.readObject();
                        for ( Entry<String, JsonValue> entry : outputs.entrySet() )
                        {
                            if( !( entry.getValue() instanceof JsonObject ) )
                                continue;
                            JsonObject fileInfo = (JsonObject) entry.getValue();
                            //TODO: put non-file results somewhere
                            if( !"File".equals( fileInfo.getString( "type", null ) ) )
                                continue;
                            String value = fileInfo.getString( "value", null );
                            if( value == null )
                                continue;
                            File resultFile = new File( value );
                            if( !resultFile.exists() )
                            {
                                log.warning( "Output file not found: " + value );
                                continue;
                            }
                            Path relPath = resultsPath2.relativize( resultFile.toPath() );
                            DataElementPath resPath = getFromRelativePath( relPath );
                            Long fileTypeId = getFileType( resultFile, FilenameUtils.getExtension( resultFile.getName() ) );
                            db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)",
                                    resultFile.getName(), fileTypeId, resPath.toString(), resultFile.length(), projectId, "workflow_runs", workflowRunId );
                        }
                    }
                    catch ( IOException e )
                    {
                        log.warning( "Failed to parse outputs.json: " + e.getMessage() );
                    }
                }
                else
                {
                    Map<String, String> allFiles = new LinkedHashMap<>();
                    collectItemsRecursive( folder, allFiles );
                    for ( Entry<String, String> file : allFiles.entrySet() )
                    {
                        File resultFile = new File( file.getValue() );
                        Long fileTypeId = getFileType( resultFile, FilenameUtils.getExtension( file.getValue() ) );
                        db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)", resultFile.getName(), fileTypeId,
                                file.getKey(), resultFile.length(), projectId, "workflow_runs", workflowRunId );
                    }
                }

            }

        }
        return "{ \"result\":\"ok\"}";
    }

    private long getFileType(File file, String fileExt)
    {
        Long typeID = db.oneLong( "SELECT id FROM file_types prj WHERE suffix = ?", fileExt );
        if( typeID != null )
            return typeID;
        String genericType = ".binary";
        if( FileTypeRegistry.isTextFile( file ) )
        {
            genericType = ".text";
        }
        typeID = db.oneLong( "SELECT id FROM file_types prj WHERE suffix = ?", genericType );
        if( typeID != null )
            return typeID;
        else
        {
            String descr = genericType.equals( ".text" ) ? "Generic text file" : "Generic binary file";
            typeID = db.insert( "INSERT INTO file_types (suffix, description) VALUES (?,?)", genericType, descr );
            return typeID;
        }
    }

    private void collectItemsRecursive(GenericFileDataCollection folder, Map<String, String> result) {
        if( folder == null || !folder.getCompletePath().exists() )
            return;
            for (String name : folder.getNameList()) {
            DataElementPath p = folder.getCompletePath().getChildPath( name );
            File f = folder.getFile( name );
                if (f != null && f.exists()) {
                result.put( p.toString(), f.getAbsolutePath() );
                }
                // descend into subfolders
                DataElement child = p.getDataElement();
                if (child instanceof GenericFileDataCollection && p.exists())
                    collectItemsRecursive( (GenericFileDataCollection) child, result );
            }
    }

    private DataElementPath getFromRelativePath(Path path)
    {
        if( path == null )
            return DataElementPath.EMPTY_PATH;
        DataElementPath dePath = DataElementPath.EMPTY_PATH;
        for ( Path component : path )
        {
            dePath = dePath.getChildPath( component.toString() );
        }
        return dePath;

    }

}
