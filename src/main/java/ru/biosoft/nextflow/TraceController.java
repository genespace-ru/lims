package ru.biosoft.nextflow;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import static com.google.common.collect.ImmutableMap.of;

import javax.json.JsonObject;
import javax.inject.Inject;
import javax.inject.Singleton;

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
    	
    	if( "POST".equals(method) && "create".equals(tokens[3]) )
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
    	String workflowId = body.getString("workflowId");
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
    	
    	return "{ \"workflowId\":\"" + workflowId + "\", \"status\":\"OK\"}";
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
