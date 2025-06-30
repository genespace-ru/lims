package ru.biosoft.lims;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import ru.biosoft.lims.controllers.LimsApiController;
import ru.biosoft.nextflow.NextflowService;

public class LimsApiModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve("/api/lims/*").with(LimsApiController.class);

        bind(LimsApiController.class).in(Scopes.SINGLETON);
        bind(NextflowService.class).in(Scopes.SINGLETON);
    }
}
