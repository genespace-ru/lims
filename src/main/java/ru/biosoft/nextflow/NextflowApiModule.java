package ru.biosoft.nextflow;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

public class NextflowApiModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve("/nf/trace/*").with(TraceController.class);
        serve( "/nf/parse/*" ).with( NextflowResultsProcessingController.class );
    	serve("/nf/*").with(NextflowController.class);

        bind(TraceController.class).in(Scopes.SINGLETON);
        bind( NextflowResultsProcessingController.class ).in( Scopes.SINGLETON );
        bind(NextflowController.class).in(Scopes.SINGLETON);
    }
}