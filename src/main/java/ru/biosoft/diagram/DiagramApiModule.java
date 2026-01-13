package ru.biosoft.diagram;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import biouml.model.DiagramInitializer;
import ru.biosoft.server.ServerInitializer;
import ru.biosoft.util.ServerPreferences;

public class DiagramApiModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve( "/diagram/*" ).with( DiagramController.class );


        bind( DiagramController.class ).in( Scopes.SINGLETON );

        ServerPreferences.loadPreferences( "pref1.xml" );
        ServerInitializer.initialize();
        DiagramInitializer.initialize();
    }


}
