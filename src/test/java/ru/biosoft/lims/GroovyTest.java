package ru.biosoft.lims;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import groovy.json.JsonSlurper;
import groovy.lang.GroovyClassLoader;

/**
 * https://groovy-lang.org/integrating.html
 */
public class GroovyTest 
{

    @Test
    public void contextTest() throws Exception
    {
        
        JsonSlurper jsonSlurper = new JsonSlurper();
        Object map = jsonSlurper.parseText( "{ \"name\": \"John Doe\" }" );        
        
        
        try(GroovyClassLoader gcl = new GroovyClassLoader())
        {
        
            Class  c = gcl.parseClass("class Foo { void doIt() { println \"ok23\" } }");    
            Method m = c.getMethod("doIt");
            Object o = c.getDeclaredConstructor().newInstance();                                                 
        
            m.invoke(o, null);
        }
    }
}


