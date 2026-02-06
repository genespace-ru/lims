package ru.biosoft.nextflow;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

@Singleton
public class TraceController extends NextflowController
{
	@Inject 
	protected DbService db;

    @Inject 
    protected NextflowService nf;
	
    @Override
    protected String process(Request req, Response res, JsonObject body) throws Exception
    {
    	String uri = req.getRequestUri();
    	String[] tokens = uri.split("/"); 

    	String method = req.getRawRequest().getMethod();
    	
        if( ("POST".equals( method ) || "GET".equals( method )) && "create".equals( tokens[3] ) )
   			return create(req, body);

    	// request: /nf/trace/workflowId/[begin/complete/heartbeat/progress]
    	if( "PUT".equals(method) && tokens[3] != null && tokens[4] != null )
    	{
    		int workflowId = Integer.parseInt(tokens[3]);
    		
    		switch(tokens[4])
    		{
    			case "begin": 	  return begin    (req, body, workflowId ); 
    			case "progress":  return progress (req, body, workflowId ); 
    			case "complete":  return complete (req, body, workflowId );
    			case "heartbeat": return heartbeat(req, body, workflowId );
    		}
    	}
    			
    	throw unknownRequest(req);
    }

    protected String create(Request req, JsonObject body) throws Exception
    {
        String workflowId = null;
        if( JsonObject.EMPTY_JSON_OBJECT.equals( body ) )
        {
            Map<String, String[]> params = req.getParameters();
            if( params.containsKey( "workflowId" ) && params.get( "workflowId" ) != null )
                workflowId = params.get( "workflowId" )[0];
            else if( params.containsKey( "workspaceId" ) && params.get( "workspaceId" ) != null )
                workflowId = params.get( "workspaceId" )[0];

        }
        else
            workflowId = body.getString( "workflowId" );
        db.updateRaw("UPDATE workflow_runs SET status='created', create_request=?::jsonb WHERE id=?",
        		body.toString(), Integer.parseInt(workflowId));

    	return "{ \"workflowId\":\"" + workflowId + "\"}";
    }

    protected String begin(Request req, JsonObject body, int workflowId) throws Exception
    {
        db.updateRaw("UPDATE workflow_runs SET status='in progress', begin_request=?::jsonb WHERE id=?",
        		body.toString(), workflowId);

    	// pending:
    	// watchUrl: "${serverUrl}/watch/${workflow.id}"
    	return "{ \"workflowId\":\"" + workflowId + "\"}";
    }

    protected String progress(Request req, JsonObject body, int workflowId) throws Exception
    {
        db.updateRaw("UPDATE workflow_runs SET status='in progress', progress_request=?::jsonb WHERE id=?",
        		body.toString(), workflowId);
        Long projectId = db.oneLong( "SELECT project FROM workflow_runs WHERE id=?", workflowId );
        JsonArray tasks = body.getJsonArray( "tasks" );
        processTasks( tasks, workflowId, projectId );
    	
    	return "{ \"workflowId\":\"" + workflowId + "\", \"status\":\"OK\"}";
    }

    private void processTasks(JsonArray tasks, int workflowId, Long projectId)
    {
        for ( int i = 0; i < tasks.size(); i++ )
        {
            JsonObject task = tasks.getJsonObject( i );
            int taskRunId = task.getInt( "taskId" );
            String status = task.getString( "status" );
            Timestamp start = getDateTime( task.get( "start" ) );
            Timestamp end = getDateTime( task.get( "complete" ) );

            /*
             * {"taskId":5,
             * "status":"NEW","hash":"b7/a07f7a","name":"multiQC","exit":2147483647,
             * "submit":null,"start":null,"process":"multiQC","tag":null,"module":[],
             * "container":"quay.io/biocontainers/multiqc:1.21--pyhdfd78af_0","attempt":1,
             * "script":"\n    multiqc .\n    ","scratch":null,
             * "workdir":"/tmp/lims_20260115092847926.tmp/3875705983264562470/work/b7/a07f7a59320ddd136e793849f7ea75",
             * "queue":null,"cpus":1,"memory":null,"disk":null,"time":null,"env":null,
             * "executor":"local","cloudZone":null,"machineType":null,"priceModel":null}
             */

            //TODO: sample - parse from parameters?

            String sql = "SELECT id FROM task_runs WHERE workflow=? AND task_run_id=?";
            Long taskId = db.oneLong( sql, workflowId, taskRunId );

            if( taskId == null )
            {
                String sqlInsert = "INSERT INTO task_runs(project, workflow, task_run_id, status) VALUES(?, ?, ?, ?)";
                taskId = (Long) db.insert( sqlInsert, projectId, workflowId, taskRunId, "scheduled" );
            }
            if( start != null )
                db.updateRaw( "UPDATE task_runs SET status=?, run_info=?::jsonb, start=?, \"end\"=? WHERE id=?", status, task.toString(), start, end, taskId );
            else
                db.updateRaw( "UPDATE task_runs SET status=?, run_info=?::jsonb, \"end\"=? WHERE id=?", status, task.toString(), end, taskId );

        }
    }

    private Timestamp getDateTime(Object dateObj)
    {
        if( dateObj == null || JsonObject.NULL.equals( dateObj ) )
            return null;
        String dateStr = null;
        if( dateObj instanceof String )
            dateStr = (String) dateObj;
        else if( dateObj instanceof JsonString )
            dateStr = ((JsonString) dateObj).getString();
        if( dateStr == null )
            return null;
        OffsetDateTime odt = OffsetDateTime.parse( dateStr );
        Instant instant = odt.toInstant();
        return Timestamp.from( instant );
    }

    protected String complete(Request req, JsonObject body, int workflowId) throws Exception
    {
    	String status = body.getJsonObject("workflow").getString("status", "succeeded");  

    	db.updateRaw("UPDATE workflow_runs SET status=?, completion_time=CURRENT_TIMESTAMP, complete_request=?::jsonb WHERE id=?",
        		status, body.toString(), workflowId);

        return "{ \"workflowId\":\"" + workflowId + "\", \"status\":\"OK\"}";
    }

    protected String heartbeat(Request req, JsonObject body, int workflowId) throws Exception
    {
        db.updateRaw( "INSERT INTO nf_heartbeat(workflow_id, heartbeat_request) VALUES (?, ?::jsonb)",
        		workflowId, body.toString());
    	
        return "{ \"workflowId\":\"" + workflowId + "\", \"status\":\"OK\"}";
    }

}
