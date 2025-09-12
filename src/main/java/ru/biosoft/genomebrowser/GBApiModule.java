package ru.biosoft.genomebrowser;

import com.developmentontheedge.beans.Preferences;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import ru.biosoft.bsa.BSAInitializer;
import ru.biosoft.server.ServerInitializer;
import ru.biosoft.util.ServerPreferences;

public class GBApiModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve( "/gb/web/login/*" ).with( LoginController.class );
        serve( "/gb/*" ).with( GBController.class );


        bind( LoginController.class ).in( Scopes.SINGLETON );
        bind( GBController.class ).in( Scopes.SINGLETON );

        //        WebProviderFactory.registerProvider( "data", new ServiceProvider() );
        //        WebProviderFactory.registerProvider( "bean", new WebBeanProvider() );
        //        WebProviderFactory.registerProvider( "img", new ImageProvider() );
        //        ServiceRegistry.registerService( "access.service", new AccessService() );
        //        ServiceRegistry.registerService( "bsa.service", new BSAService() );
        //        BeanRegistry.registerBeanProvider( "bsa/siteviewoptions", "ru.biosoft.bsa.server.SiteViewOptionsProvider" );
        //        TransformerRegistry.addTransformer( "BedFile", "ru.biosoft.bsa.transformer.BedFileTransformer", "ru.biosoft.access.file.FileDataElement", "ru.biosoft.bsa.track.BedTrack" );
        //        FileTypeRegistry
        //                .register( new FileType( "BED track", new String[] { "bed" }, "ru.biosoft.bsa.transformer.BedFileTransformer", FileTypePriority.HIGH_PRIORITY, "BED track file" ) );
        //ServerPreferences.setPreferences( new Preferences() );
        ServerPreferences.loadPreferences( "pref1.xml" );
        ServerInitializer.initialize();
        BSAInitializer.initialize();
    }


}
