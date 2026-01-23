package ru.biosoft.lims;

import com.codahale.metrics.jmx.JmxReporter;
import com.developmentontheedge.be5.modules.core.CoreModule;
import com.developmentontheedge.be5.modules.core.CoreServletModule;
import com.developmentontheedge.be5.modules.monitoring.MetricsModule;
import com.developmentontheedge.be5.web.WebModule;
import com.developmentontheedge.be5.server.servlet.Be5ServletListener;
import com.developmentontheedge.be5.server.servlet.TemplateModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static com.developmentontheedge.be5.modules.monitoring.Metrics.METRIC_REGISTRY;

import ru.biosoft.genomebrowser.GBApiModule;
import ru.biosoft.lims.repository.RepositoryModule;
import ru.biosoft.nextflow.NextflowApiModule;
import ru.biosoft.webserver.WebserverApiModule;

public class AppGuiceServletConfig extends Be5ServletListener
{
    @Override
    protected Injector getInjector()
    {
        return Guice.createInjector(getStage(), new AppModule());
    }

    private static class AppModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new CoreModule());
            install(new CoreServletModule());
            install(new WebModule());
            install(new TemplateModule());
            install(new MetricsModule());

            install(new LimsApiModule());
            install(new NextflowApiModule());
            install(new RepositoryModule());
            install( new GBApiModule() );
            install( new WebserverApiModule() );

            final JmxReporter jmxReporter = JmxReporter.forRegistry(METRIC_REGISTRY).build();
            jmxReporter.start();
            
            requestStaticInjection(SystemSettings.class);

//            final Graphite graphite = new Graphite(new InetSocketAddress("127.0.0.1", 2003));
//            final GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(METRIC_REGISTRY)
//                    .prefixedWith("testApp.com")
//                    .convertRatesTo(TimeUnit.SECONDS)
//                    .convertDurationsTo(TimeUnit.MILLISECONDS)
//                    .filter(MetricFilter.ALL)
//                    .build(graphite);
//            graphiteReporter.start(1, TimeUnit.MINUTES);
        }
    }
}
