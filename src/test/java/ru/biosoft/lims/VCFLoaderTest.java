package ru.biosoft.lims;

import java.io.File;

import org.junit.jupiter.api.Test;

import ru.biosoft.lims.loader.VCFLoader;

/**
 * Tests for VCF and VEP loading for test project lims-test-hemotology.
 * 
 *  Test tables are created with suffix <code>test</test> to not interfere with project tables.
 */
public class VCFLoaderTest extends DbTest
{
	protected static String PROJECT = "test-hematology";
	protected static String SAMPLE  = "sample1";
	
    @Test
    public void checkFormat() throws Exception
    {
    	VCFLoader loader = getInjector().getInstance(VCFLoader.class);
    	File file = new File(loader.getProjectDir() + PROJECT + "/results/VCF/" + SAMPLE + ".vcf");

        loader.checkFormat(file);
    }
   
}