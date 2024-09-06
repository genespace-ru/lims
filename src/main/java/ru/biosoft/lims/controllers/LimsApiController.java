package ru.biosoft.lims.controllers;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.servlet.support.ApiControllerSupport;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.be5.config.CoreUtils;

import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.util.Either;

import com.developmentontheedge.be5.databasemodel.DatabaseModel;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServletRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.ImmutableMap.of;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.JsonReader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Singleton
public class LimsApiController extends ApiControllerSupport
{
    private static final Logger log = Logger.getLogger( LimsApiController.class.getName() );

    @Inject private DatabaseModel database;

    @Inject protected Meta meta;
    @Inject private OperationService operationService;
    @Inject private OperationExecutor operationExecutor;

    @Inject private CoreUtils coreUtils;

    @Override
    protected void generate( Request req, Response res, String subUrl )
    {        
        log.info( "Context path: \"" + req.getContextPath() + "\" subUrl: \"" + subUrl + "\"" );

        HttpServletRequest httpReq = req.getRawRequest();
        String resultContent = "{\"type\":\"msg\",\"status\":\"warning\",\"content\":\"Unknown error\"}";
        try
        {
            String bodyIncoming = req.getBody();
            JsonReader jsonReader = Json.createReader( new StringReader( bodyIncoming ) );
            JsonObject body = jsonReader.readObject();
            String work = body.getString( "work" );

            OperationInfo operationInfo = null; 

            try
            {             
                operationInfo = new OperationInfo( meta.getOperation( "_api_", "All records", work ) );
            }
            catch( Exception exc )
            {
                //exc.printStackTrace( System.err );
                log.log( Level.SEVERE, subUrl, exc );
                resultContent = "{\"type\":\"msg\",\"status\":\"warning\",\"content\":\"Unknown work type '" + work + "'\"}";  
                res.sendJson( resultContent );
                return; 
            }

            OperationContext operationContext = operationExecutor.getOperationContext(
                    operationInfo, "All records", new HashMap() );

            Operation operation = operationExecutor.create(operationInfo, operationContext);

            if( operation instanceof com.developmentontheedge.be5.server.operations.support.OperationSupport )
            {
               ( ( com.developmentontheedge.be5.server.operations.support.OperationSupport )operation ).setRequest( req );
            }

            HashMap presetValues = new HashMap();
            DynamicPropertySet params = ( DynamicPropertySet )operationExecutor.generate( operation, presetValues );
            params.setValue( "json", bodyIncoming );
            presetValues.put( "json", bodyIncoming );
            Either<Object, OperationResult> result = operationService.execute( operation, presetValues );
            if( operation.getResult().getDetails() instanceof Throwable )
            {
                Throwable t = ( Throwable  )operation.getResult().getDetails();
                Throwable origT = t;
                while( t.getCause() != null && t.getCause().getMessage() != null )
                {
                    t = t.getCause();
                }
                String errMsg = t.getMessage();
                String outMsg = errMsg.replace("\"", "\\\"").replace("\n", "\\n");
                outMsg = t.getClass().getSimpleName() + ": " + outMsg;  
    
                resultContent = "{\"type\":\"msg\",\"status\":\"warning\",\"content\":\"" + outMsg + "\"}";
                res.sendJson( resultContent );
            }
            else
            {
                resultContent = operation.getResult().getMessage();
            }
        }
        catch( Exception exc )
        {
            log.log( Level.SEVERE, "" + subUrl, exc );

            String errMsg = exc.getMessage();
            String outMsg = errMsg.replace("\"", "\\\"").replace("\n", "\\n");

            resultContent = "{\"type\":\"msg\",\"status\":\"warning\",\"content\":\"" + outMsg + "\"}";
        }          

        res.sendJson( resultContent );
    }
}
