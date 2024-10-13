package ru.biosoft.lims.loader;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VCFLoader loads SNV from VCFv4.2 file into corresponding database table.
 *
 * Not parsed fields:
 * 
 * 1.2.8 Sample field format
 * ##SAMPLE=<ID=S_ID,Genomes=G1_ID;G2_ID; ...;GK_ID,Mixture=N1;N2; ...;NK,Description=S1;S2; ...;SK>
 * 
 * 1.2.9 Pedigree field format
 * ##PEDIGREE=<Name_0=G0-ID,Name_1=G1-ID,...,Name_N=GN-ID>
 * or a link to a database:
 * ##pedigreeDB=URL
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
	
	/**
	 * Returns key value.
	 * Expected format:
	 * <code><[...,]key=["]value["][,...]>
	 * ##fieldName=...</code>
	 */
	protected String getValue(String line, String key, boolean optional) throws Exception
	{
		int start = line.indexOf(key+'=');
		if( start == -1 )
		{
			if ( ! optional)
				throw new Exception("Field format error, key=" + key +" is missing.");
			else
				return "null";
		}
		
		start += key.length() + 1;
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
		
		// 1.2.1 File format
		if( "fileformat".equalsIgnoreCase(field) )
		{
			insertMeta(field, value);
			return;
		}

		// 1.2.2 Information field format
		// INFO fields should be described as follows (first four keys are required, source and version are recommended):
		// ##INFO=<ID=ID,Number=number,Type=type,Description="description",Source="source",Version="version">
		if( "INFO".equalsIgnoreCase(field) )
		{
			String id          = getValue(value, "ID",     false); 
			String number      = getValue(value, "Number", false); 
			String type        = getValue(value, "Type",   false); 
			String description = getValue(value, "Description", false); 

			db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, number, type, description) " +
			              "VALUES('INFO', ?, ?, ?, ?, ?)", value, id, number, type, description);
			return;
		}
		
		// 1.2.3 Filter field format
		// ##FILTER=<ID=ID,Description="description">		
		if( "FILTER".equalsIgnoreCase(field) )
		{
			String id = getValue(value, "ID", false); 
			String description = getValue(value, "Description", false); 

			db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, description) VALUES('FILTER', ?, ?, ?)", value, id, description);
			return;
		}

		// 1.2.4 Individual format field format
		// ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
		// fields specified in the FORMAT field should be described as follows:
		// ##FORMAT=<ID=ID,Number=number,Type=type,Description="description">
		// Possible Types for FORMAT fields are: Integer, Float, Character, and String 
		if( "FORMAT".equalsIgnoreCase(field) )
		{
			String id          = getValue(value, "ID",     false); 
			String number      = getValue(value, "Number", false); 
			String type        = getValue(value, "Type",   false); 
			String description = getValue(value, "Description", false); 

			db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, number, type, description) " +
			              "VALUES('FORMAT', ?, ?, ?, ?, ?)", value, id, number, type, description);
			return;
		}
		
		// 1.2.5 Alternative allele field format
		// Symbolic alternate alleles for imprecise structural variants:
		// ##ALT=<ID=type,Description="description">
		if( "ALT".equalsIgnoreCase(field) )
		{
			String id = getValue(value, "ID", false); 
			String description = getValue(value, "Description", false); 

			db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, description) VALUES('ALT', ?, ?, ?)", value, id, description);
			return;
		}

		// 1.2.6 Assembly field format
		// Breakpoint assemblies for structural variations may use an external file:
		// ##assembly=url
		if( "assembly".equalsIgnoreCase(field) )
		{
			insertMeta(field, value);
			return;
		}
		
		// 1.2.7 Contig field format
		// ##contig=<ID=ctg1,URL=ftp://somewhere.org/assembly.fa,...>		
		if( "contig".equalsIgnoreCase(field) )
		{
			String id = getValue(value, "ID", false); 
			String url = getValue(value, "URL", true); 
			
			db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, url) " +
			              "VALUES('contig', ?, ?, ?)", value, id, url);
			return;
		}

		// other tags:
		insertMeta(field, value);
	}
	
	protected void processHeader(String line) throws Exception	{}
	
	protected void processSNV(String line) throws Exception		{}
}