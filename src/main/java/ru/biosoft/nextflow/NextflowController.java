package ru.biosoft.nextflow;

import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.server.servlet.support.BaseControllerSupport;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NextflowController extends BaseControllerSupport
{
	private static final Logger log = Logger.getLogger(NextflowController.class.getName() );

    @Inject protected DatabaseModel database;

	public static String generateRandomString(int len)
	{
	    int leftLimit = 48; // numeral '0'
	    int rightLimit = 122; // letter 'z'
	    Random random = new Random();

	    return random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(len)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();
	}
    
    protected Exception unknownRequest(Request req)
    {
    	return new RuntimeException("Unsupported command: " + req.getRawRequest().getMethod() + " " + req.getRequestUri());
    }
    
    protected String process(Request req, Response res, JsonObject body) throws Exception
    {
    	throw unknownRequest(req);
    }

    @Override
    public void generate(Request req, Response res)
    {
    	log.info( "Nextflow request: " + req.getRawRequest().getMethod() + " " + req.getRequestUri());
    	
    	String json;
    	try
    	{
            String bodyIncoming = req.getBody();
            log.info( "Nextflow request body: " + bodyIncoming );
            JsonObject body = JsonObject.EMPTY_JSON_OBJECT;
            if(bodyIncoming == null || bodyIncoming.isBlank())
            {
                log.severe( "Nextflow request body is empty" );
            }
            else
            {
                JsonReader jsonReader = Json.createReader( new StringReader( bodyIncoming ) );
                body = jsonReader.readObject();
            }
        	log.info("  json: " + body);
    		
    		json = process(req, res, body);
    		res.sendJson(json);
    		log.info("Response: " + json);
    	}
        catch(Throwable t)
        {
            String message = t.getMessage() != null ? t.getMessage() : "unknown reason";
            log.log( Level.SEVERE, "Nextflow service exeption: " + message, t );

            String error = message;
            if( t.getCause() != null )
                error += ", cause: " + t.getCause().getMessage()
            					.replace("\"", "\\\"").replace("\n", "\\n");
            res.setStatus(400);
            res.sendJson("{ \"message\":\"" + error + "\"}");
        }          
    }
}
