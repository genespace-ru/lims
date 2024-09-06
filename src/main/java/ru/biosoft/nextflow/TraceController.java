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
	@Inject protected DbService db;
	
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
    		switch(tokens[4])
    		{
    			case "begin": 	  return begin(req, body, tokens[3]); 
    			case "progress":  return progress(req, body, tokens[3]); 
    			case "complete":  return complete(req, body, tokens[3]);
    			case "heartbeat": return heartbeat(req, body, tokens[3]);
    		}
    	}
    			
    	throw unknownRequest(req);
    }

    protected String create(Request req, JsonObject body) throws Exception
    {
    	String workflowId = generateRandomString(8);

        db.updateRaw( "INSERT INTO nf_workflows (workflow_id, status, create_request) VALUES (?, ?, ?::jsonb)",
        		workflowId, "CREATED", body.toString());

    	return "{ \"workflowId\":\"" + workflowId + "\"}";
    }

    protected String begin(Request req, JsonObject body, String workflowId) throws Exception
    {
    	EntityModel<Long> nf_workflows = database.getEntity("nf_workflows");
    	Long id = nf_workflows.getBy(of("workflow_id", workflowId)).getPrimaryKey();
        db.updateRaw("UPDATE nf_workflows SET status='SUBMITTED', begin_request=?::jsonb WHERE id=?",
        		body.toString(), id);

    	// pending:
    	// watchUrl: "${serverUrl}/watch/${workflow.id}"
    	return "{ \"workflowId\":\"" + workflowId + "\"}";
    }

    protected String progress(Request req, JsonObject body, String workflowId) throws Exception
    {
    	EntityModel<Long> nf_workflows = database.getEntity("nf_workflows");
    	Long id = nf_workflows.getBy(of("workflow_id", workflowId)).getPrimaryKey();
        db.updateRaw("UPDATE nf_workflows SET progress_request=?::jsonb WHERE id=?",
        		body.toString(), id);
    	
    	return "{ \"workflowId\":\"" + workflowId + "\", \"status\":\"OK\"}";
    }

    protected String complete(Request req, JsonObject body, String workflowId) throws Exception
    {
    	EntityModel<Long> nf_workflows = database.getEntity("nf_workflows");
    	Long id = nf_workflows.getBy(of("workflow_id", workflowId)).getPrimaryKey(); 
    	String status = body.getJsonObject("workflow").getString("status", "SUCCEEDED");  

    	db.updateRaw("UPDATE nf_workflows SET status=?, completion_time=CURRENT_TIMESTAMP, complete_request=?::jsonb WHERE id=?",
        		status, body.toString(), id);

        return "{ \"workflowId\":\"" + workflowId + "\", \"status\":\"OK\"}";
    }

    protected String heartbeat(Request req, JsonObject body, String workflowId) throws Exception
    {
        db.updateRaw( "INSERT INTO nf_heartbeat(workflow_id, heartbeat_request) VALUES (?, ?::jsonb)",
        		workflowId, body.toString());
    	
        return "{ \"workflowId\":\"" + workflowId + "\", \"status\":\"OK\"}";
    }

}
