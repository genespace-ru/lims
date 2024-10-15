package ru.biosoft.lims;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import ru.biosoft.lims.loader.VCFLoader;
import ru.biosoft.lims.loader.VEPLoader;

/**
 * Tests for VCF and VEP loading for test project lims-test-hemotology.
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VCFLoaderTest extends DbTest
{
	protected static String PROJECT = "test-hematology";
	protected static String SAMPLE  = "sample1";
	
	VCFLoader vcfLoader;
    VEPLoader vepLoader;
	File file;
	
	@Disabled
	@Test
    @Order(1) 
    public void checkVcfFormat() throws Exception
    {
    	vcfLoader = getInjector().getInstance(VCFLoader.class);
    	file = new File(vcfLoader.getProjectDir() + PROJECT + "/results/VCF/" + SAMPLE + ".vcf");

    	vcfLoader.checkFormat(file);
    }

    @Disabled
    @Test
    @Order(2) 
    public void checkLoadVCF() throws Exception
    {
        try 
        {
            vcfLoader.load(file, PROJECT, SAMPLE);
        }
        catch(Exception t)
        {
            // for debugging
            throw t;
        }
    }
    
    @Disabled
    @Test
    @Order(3)
    public void checkLoadGenominal() throws Exception
    {
        try 
        {
            file = new File(vcfLoader.getProjectDir() + PROJECT + "/results/VCF/" + SAMPLE + "_genomenal.vcf");
            vcfLoader.load(file, PROJECT, SAMPLE+"_genomenal");
        }
        catch(Exception t)
        {
            // for debugging
            throw t;
        }
    }
    
    @Test
    @Order(4)
    public void checkLoadVEP() throws Exception
    {
        try 
        {
            vepLoader = getInjector().getInstance(VEPLoader.class);
            file = new File(vepLoader.getProjectDir() + PROJECT + "/results/VEP/" + SAMPLE + ".vep");

            vepLoader.checkFormat(file);
            vepLoader.load(file, PROJECT, SAMPLE);
        }
        catch(Exception t)
        {
            // for debugging
            throw t;
        }
    }
    
    
    
}