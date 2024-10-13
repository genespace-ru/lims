package ru.biosoft.lims;

import java.io.File;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import ru.biosoft.lims.loader.VCFLoader;

/**
 * Tests for VCF and VEP loading for test project lims-test-hemotology.
 * 
 *  Test tables are created with suffix <code>test</test> to not interfere with project tables.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class VCFLoaderTest extends DbTest
{
	protected static String PROJECT = "test-hematology";
	protected static String SAMPLE  = "sample1";
	
	VCFLoader loader;
	File file;
	
    @Test
    @Order(1) 
    public void checkFormat() throws Exception
    {
    	loader = getInjector().getInstance(VCFLoader.class);
    	file = new File(loader.getProjectDir() + PROJECT + "/results/VCF/" + SAMPLE + ".vcf");

        loader.checkFormat(file);
    }

    @Test
    @Order(2) 
    public void checkLoad() throws Exception
    {
        loader.load(file, PROJECT, SAMPLE);
    }
}