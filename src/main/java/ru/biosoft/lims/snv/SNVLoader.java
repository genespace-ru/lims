package ru.biosoft.lims.snv;

import com.developmentontheedge.be5.database.DbService;
import javax.inject.Inject;

/**
 * Utility class to load SNV data from VCF or VEP files into corresponding database tables.
 */
public class SNVLoader
{
    @Inject 
    private DbService db;

    protected String getProjectDir()
    {
    	return db.getString("SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='projects_dir'");
    }
    
    
    public void loadVCF(String sampleName)
    {
    	String dir = getProjectDir();
    	System.out.println("SNV loader, dir=" + dir);
    }
	
}