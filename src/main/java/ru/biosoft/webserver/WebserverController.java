package ru.biosoft.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.server.servlet.support.BaseControllerSupport;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.Session;

import ru.biosoft.access.core.Environment;
import ru.biosoft.lims.repository.RepositoryManager;
import ru.biosoft.server.servlets.webservices.JSONResponse;
import ru.biosoft.server.servlets.webservices.WebServicesServlet;
import ru.biosoft.util.TextUtil2;

public class WebserverController extends BaseControllerSupport
{
    private static final Logger log = Logger.getLogger( WebserverController.class.getName() );
    private final static WebServicesServlet servlet = new WebServicesServlet();

    @Inject
    private RepositoryManager repo;

    @Override
    public void generate(Request request, Response response)
    {
        log.info( "WebServer request: " + request.getRawRequest().getMethod() + " " + request.getRequestUri() );
        try
        {
            Session session = request.getSession();
            String method = request.getRawRequest().getMethod();
            if( !method.equals( "GET" ) && !method.equals( "HEAD" ) && !method.equals( "POST" ) )
            {
                throw new IllegalArgumentException( method + " method not supported" );
            }

            //TODO: make this call somewhere else in static initializer
            if( repo != null )
                repo.getRepositoryPath();

            String target = request.getRequestUri();
            Object docRootObj = Environment.getValue( "DocRoot" );
            String docRoot = docRootObj != null ? docRootObj.toString() : "";
            if( !target.startsWith( docRoot ) )
            {
                sendNotFound( response, target );
                return;
            }

            Map<String, String[]> uriParameters = request.getParameters();

            final String subTarget = target.substring( 1 );
            final Map<String, Object> arguments = new HashMap<>();
            response.setStatus( HttpServletResponse.SC_OK );

            final OutputStream out = response.getOutputStream();
            try
            {
                for ( String uriParameter : uriParameters.keySet() )
                {

                    arguments.put( TextUtil2.decodeURL( uriParameter ), uriParameters.get( uriParameter ) );
                }

                //TODO: modified
                //servlet.service(subTarget, session, arguments, out, new ServerHttpResponseWrapper(response));
                servlet.service( subTarget, session.getRawSession(), arguments, out, response.getRawResponse() );

            }
            catch (Throwable t)
            {
                new JSONResponse( out ).error( t );
            }

        }
        catch (IOException e)
        {
            log.severe( "Error processing request " + request.getRawRequest().getMethod() + " " + request.getRequestUri() + " Error: " + e.getMessage() );
        }
    }



    private static void sendForbidden(Response response) throws IOException
    {
        response.setStatus( HttpServletResponse.SC_FORBIDDEN );
        String forbiddenMessage = "<html><body><h1>Access to this server is forbidden</h1></body></html>";
        response.sendHtml( forbiddenMessage );
    }

    private void sendNotFound(Response response, String target) throws IOException
    {
        response.setStatus( HttpServletResponse.SC_NOT_FOUND );
        String forbiddenMessage = "<html><body><h1>The requested URL " + target + " is not found</h1></body></html>";
        response.sendHtml( forbiddenMessage );
    }

}
