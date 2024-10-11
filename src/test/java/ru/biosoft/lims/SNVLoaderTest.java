package ru.biosoft.lims;

import org.junit.Test;
import ru.biosoft.lims.snv.SNVLoader;

/**
 * Tests for VCF and VEP loading for test project lims-test-hemotology.
 * 
 *  Test tables are created with suffix <code>test</test> to not interfere with project tables.
 */
public class SNVLoaderTest extends DbTest
{
	protected static String SAMPLE = "test";
	
    @Test
    public void test()
    {
        SNVLoader loader = getInjector().getInstance(SNVLoader.class);
        loader.loadVCF(SAMPLE);
    }

}