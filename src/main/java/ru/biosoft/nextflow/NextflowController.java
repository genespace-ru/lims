package ru.biosoft.nextflow;

import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.server.servlet.support.BaseControllerSupport;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    	String uri = req.getRequestUri();
    	log.info( "Nextflow request: " + req.getRawRequest().getMethod() + " " + req.getRequestUri());
    	
    	String[] tokens = uri.split("/");
    	String json;
    	try
    	{
            String bodyIncoming = req.getBody();
            JsonReader jsonReader = Json.createReader( new StringReader( bodyIncoming ) );
            JsonObject body = jsonReader.readObject();
        	log.info("  json: " + body);
    		
    		json = process(req, res, body);
    		res.sendJson(json);
    		log.info("Response: " + json);
    	}
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "Nextflow service exeption: " + t.getMessage(), t);

            String error = t.getMessage() + ", cause: " + t.getCause().getMessage()
            					.replace("\"", "\\\"").replace("\n", "\\n");
            res.setStatus(400);
            res.sendJson("{ \"message\":\"" + error + "\"}");
        }          
    }
}
