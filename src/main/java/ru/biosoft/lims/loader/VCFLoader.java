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

	protected void insertFilter(String value, String id, String description)
	{
		db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, description) VALUES('FILTER', ?, ?, ?)", value, id, description);
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
	
	/**
	 * Returns key value.
	 * Expected format:
	 * <code><[...,]key=["]value["][,...]>
	 * ##fieldName=...</code>
	 */
	protected String getValue(String line, String key, boolean optional) throws Exception
	{
		int start = line.indexOf(key+'=') + key.length() + 1;
		if( start == -1 && ! optional)
			throw new Exception("Field format error, key=" + key +" is missing.");
		
		int end;
		if( line.charAt(start) == '"' )
		{
			start++;
			end = line.indexOf('"', start+1);
		}
		else
		{
			end = line.indexOf(',', start+1);
			if( end == -1 )
				end = line.indexOf('>', start+1);
		}
		if( end == -1 )
			throw new Exception("Field end undefined, key=" + key +".");

		return line.substring(start, end);
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

		//##FILTER=<ID=PASS,Description="All filters passed">
		if( "FILTER".equalsIgnoreCase(field) )
		{
			String id = getValue(value, "ID", false); 
			String description = getValue(value, "Description", false); 

			insertFilter(value, id, description);
		}
		
	}
	
	protected void processHeader(String line) throws Exception	{}
	
	protected void processSNV(String line) throws Exception		{}
}