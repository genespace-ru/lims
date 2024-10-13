package ru.biosoft.lims.loader;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VCFLoader loads SNV from VCFv4.2 file into corresponding database table.
 */
public class VCFLoader extends LoaderSupport
{
    public static final Logger log = Logger.getLogger(VCFLoader.class.getName());

	public String getFormat()			{ return "VCF"; }
	public String getVersion()			{ return "4.2"; }
	public String getFormatFirstLine()	{ return "##fileformat=VCFv4.2"; }

	protected String snvTableName;
	protected String metaTableName;
	
	protected void preload(File file, String project, String sample) 
	{
		snvTableName  = "snv_"      + sample;
		metaTableName = "snv_meta_" + sample;
		
		db.updateRaw("DROP TABLE IF EXISTS " + snvTableName);
		db.updateRaw("DROP TABLE IF EXISTS " + metaTableName);
		db.updateRaw("CREATE TABLE " + snvTableName  + " (LIKE snv_      INCLUDING defaults INCLUDING constraints INCLUDING indexes)");
		db.updateRaw("CREATE TABLE " + metaTableName + " (LIKE snv_meta_ INCLUDING defaults INCLUDING constraints INCLUDING indexes)");
	}

	protected void insertMeta(String section, String value)
	{
		db.updateRaw("INSERT INTO " + metaTableName + "(section, value) VALUES(?, ?)", section, value);
	}
	
	protected void processLine(String line)
	{
		try
		{
			if( line.startsWith("##") )
			{
				processMeta(line);
				return;
			}
		
			if( line.startsWith("#") )
			{
				processHeader(line);
				return;
			}
		
			processSNV(line);
		}
		catch(Throwable t)
		{
			log.log(Level.SEVERE, "VCF parsing error: " + t + System.lineSeparator() +
				"line: " + lineNumber + System.lineSeparator() + 
				line);
		}
	}

	/**
	 * Returns fieldName.
	 * Expected format:
	 * <code>##fieldName=...</code>
	 */
	protected String getField(String line) throws Exception
	{
		int end = line.indexOf('=', 2);
		if( end == -1 )
			throw new Exception("Field format error, '=' is missing.");
		
		return line.substring(2, end);
	}
	
	protected void processMeta(String line) throws Exception
	{
		String field = getField(line);
		String value = line.substring(3+field.length()).trim(); 
		
		if( "fileformat".equalsIgnoreCase(field) )
		{
			insertMeta(field, value);
			return;
		}
	}
	
	protected void processHeader(String line) throws Exception	{}
	
	protected void processSNV(String line) throws Exception		{}
}