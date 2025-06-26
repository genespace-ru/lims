package ru.biosoft.lims.repository;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import ru.biosoft.access.core.Environment;
import ru.biosoft.util.SimpleClassLoading;

public class RepositoryModule extends ServletModule
{

    @Override
    protected void configureServlets()
    {
        Environment.setClassLoading( new SimpleClassLoading() );
        bind( RepositoryManager.class ).in( Scopes.SINGLETON );
    }

}
