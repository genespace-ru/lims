package ru.biosoft.lims;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import ru.biosoft.lims.controllers.LimsApiController;

public class LimsApiModule extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve("/api/lims/*").with(LimsApiController.class);

        bind(LimsApiController.class).in(Scopes.SINGLETON);
    }
}
