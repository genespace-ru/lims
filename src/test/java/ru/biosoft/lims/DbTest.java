package ru.biosoft.lims;

import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.modules.core.CoreModule;
import com.developmentontheedge.be5.modules.core.CoreServletModule;
import com.developmentontheedge.be5.web.WebModule;
import com.developmentontheedge.be5.test.TestUtils;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.google.inject.AbstractModule;

public class DbTest extends TestUtils 
{
    private static final Injector injector = initInjector(
            Modules.override(new CoreModule()).with(new DbTestModule()) );
    
    public Injector getInjector()
    {
        return injector;
    }

}
