package ru.biosoft.nextflow;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import ru.biosoft.access.core.DataElementPath;
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
            return parseResults(req, body);

        throw unknownRequest(req);
    }

    private String parseResults(Request req, JsonObject body)
    {
        Integer workflowId = body.getInt( "workflowId" );
        Integer projectId = body.getInt( "prjId" );
        String repoPath = repo.getRepositoryPath();

        String projectName = db.getString( "SELECT name FROM projects WHERE ID=?", projectId );
        //DataElementPath projectPath = DataElementPath.create( repoPath ).getChildPath( projectName );
        //DataElementPath results = projectPath.getChildPath( "results" );
        //GenericFileDataCollection resultsDc = (GenericFileDataCollection) results.getDataCollection();


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
                        db.updateRaw( sql, projectId, sampleId, fileInfo, workflowId, params.toString() );
                    }
                }

            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return "{ \"workflowId\":\"" + workflowId + "\"}";

    }

}
