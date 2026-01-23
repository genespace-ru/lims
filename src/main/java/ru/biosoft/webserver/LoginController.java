package ru.biosoft.webserver;

import com.developmentontheedge.be5.server.servlet.support.BaseControllerSupport;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

public class LoginController extends BaseControllerSupport
{

    @Override
    public void generate(Request req, Response res)
    {
        //TODO: check user or session os something security-related
        res.sendJson( "Login ok" );
    }

}
