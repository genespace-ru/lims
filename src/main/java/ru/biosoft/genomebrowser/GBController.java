package ru.biosoft.genomebrowser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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

public class GBController extends BaseControllerSupport
{
    private static final Logger log = Logger.getLogger( GBController.class.getName() );
    private final static WebServicesServlet servlet = new WebServicesServlet();
    @Inject
    private RepositoryManager repo;
    //TODO: get session from somewhere
    //private final static SystemSession session = new SystemSession();

    //TODO: session Id should be taken from current session
    //private final String sessionId;
    @Override
    public void generate(Request req, Response res)
    {
        String uri = req.getRequestUri();
        log.info( "GenomeBrowser request: " + req.getRawRequest().getMethod() + " " + req.getRequestUri() );
        // String[] tokens = uri.split( "/" );
        //String json = "Genome Browser will be here";
        //res.sendJson( json );
        try
        {
            Session session = req.getSession();
            String id = session.getSessionId();
            handle( req.getRawRequest(), res.getRawResponse(), "POST", null, session );
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //log.info( "Response: " + json );
    }

    public void handle(final HttpServletRequest request, final HttpServletResponse response, String method, Map<String, Object> extraArguments, Session session)
            throws IOException
    {
        if( !method.equals( "GET" ) && !method.equals( "HEAD" ) && !method.equals( "POST" ) )
        {
            throw new IllegalArgumentException( method + " method not supported" );
        }
        String sessionId = "";
        if( request.getCookies() != null )
        {
            for ( Cookie cookie : request.getCookies() )
            {
                if( cookie.getName().equals( "JSESSIONID" ) )
                    sessionId = cookie.getValue();
            }
        }
        //TODO: sessionId has .node0 at the end
        if( !sessionId.startsWith( session.getSessionId() ) )
        {
            sendForbidden( response );
            return;
        }

        //TODO: make this call somewhere else in static initializer
        if( repo != null )
            repo.getRepositoryPath();
        String target = request.getRequestURI();
        Object docRootObj = Environment.getValue( "DocRoot" );
        String docRoot = docRootObj != null ? docRootObj.toString() : "";
        if( !target.startsWith( docRoot ) )
        {
            sendNotFound( response, target );
            return;
        }

        int pos = target.indexOf( '?' );
        Map<String, String[]> uriParameters = request.getParameterMap();

        final String subTarget = target.substring( 1 );
        final Map<String, Object> arguments = new HashMap<>();
        response.setStatus( HttpServletResponse.SC_OK );

        final ServletOutputStream out = response.getOutputStream();//new ByteArrayOutputStream();
        try
        {
            //            if( FileUpload.isMultipartContent( request ) )
            //            {
            //
            //                FileItemFactory factory = new DiskFileItemFactory();
            //                FileUpload upload = new FileUpload( factory );
            //                //JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);
            //                //upload.setFileSizeMax(MAX_FILE_SIZE);
            //                //upload.setSizeMax(MAX_REQUEST_SIZE);
            //                //                String uploadPath = request.getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY;
            //                //                File uploadDir = new File(uploadPath);
            //                //                if (!uploadDir.exists()) {
            //                //                    uploadDir.mkdir();
            //                //                }
            //
            //                try
            //                {
            //                    List<FileItem> formItems = upload.parseRequest( request );
            //
            //                    if( formItems != null && formItems.size() > 0 )
            //                    {
            //                        for ( FileItem item : formItems )
            //                        {
            //                            if( item.isFormField() )
            //                            {
            //                                String name = item.getFieldName();
            //                                String value = item.getString();
            //                                arguments.put( name, new String[] { value } );
            //                            }
            //                            else
            //                            {
            //                                String name = item.getFieldName();
            //                                arguments.put( name, new FileItem[] { item } );
            //                            }
            //                            //                            if (!item.isFormField()) {
            //                            //                                String fileName = new File(item.getName()).getName();
            //                            //                                item.write(Path.of(uploadPath, fileName));
            //                            //                                request.setAttribute("message", "File " + fileName + " has uploaded successfully!");
            //                            //                            }
            //                        }
            //                    }
            //                }
            //                catch (Exception ex)
            //                {
            //                    //request.setAttribute("message", "There was an error: " + ex.getMessage());
            //                }
            //                //getServletContext().getRequestDispatcher("/result.jsp").forward(request, response);
            //            }
            //            else
            //            {
            //                //???uriParameters.addAll(Arrays.asList(TextUtil.split(new String(EntityUtils.toByteArray(entity)), '&')));
            //            }
            for ( String uriParameter : uriParameters.keySet() )
            {

                arguments.put( TextUtil2.decodeURL( uriParameter ), uriParameters.get( uriParameter ) );
            }
            if( extraArguments != null )
            {
                arguments.putAll( extraArguments );
            }

            //TODO: modified
            //servlet.service(subTarget, session, arguments, out, new ServerHttpResponseWrapper(response));
            servlet.service( subTarget, session.getRawSession(), arguments, out, response );

        }
        catch (Throwable t)
        {
            new JSONResponse( out ).error( t );
        }

        //TODO: did the out of the response send fully?
        //        response.setEntity(new ByteArrayEntity(out.toByteArray()));
        response.flushBuffer();

    }

    private static void sendForbidden(HttpServletResponse response) throws IOException
    {
        response.setStatus( HttpServletResponse.SC_FORBIDDEN );
        String forbiddenMessage = "<html><body><h1>Access to this server is forbidden</h1></body></html>";
        response.setContentType( "text/html; charset=UTF-8" );
        response.getWriter().println( forbiddenMessage );
    }

    private void sendNotFound(final HttpServletResponse response, final String target) throws IOException
    {
        response.setStatus( HttpServletResponse.SC_NOT_FOUND );
        String forbiddenMessage = "<html><body><h1>The requested URL " + target + " is not found</h1></body></html>";
        response.setContentType( "text/html; charset=UTF-8" );
        response.getWriter().println( forbiddenMessage );
    }

}
