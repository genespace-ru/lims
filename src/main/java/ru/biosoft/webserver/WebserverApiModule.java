package ru.biosoft.webserver;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import ru.biosoft.access.AccessCoreInit;
import ru.biosoft.access.AccessInitializer;
import ru.biosoft.bsa.BSAInitializer;
import ru.biosoft.server.ServerInitializer;
import ru.biosoft.util.ServerPreferences;

public class WebserverApiModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve( "/webserver/web/login/*" ).with( LoginController.class );
        serve( "/webserver/*" ).with( WebserverController.class );


        bind( LoginController.class ).in( Scopes.SINGLETON );
        bind( WebserverController.class ).in( Scopes.SINGLETON );

        AccessCoreInit.init();
        ServerPreferences.loadPreferences( "preferences.xml" );
        AccessInitializer.initialize();
        ServerInitializer.initialize();
        BSAInitializer.initialize();
    }


}
