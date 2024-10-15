package ru.biosoft.lims.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.developmentontheedge.be5.database.DbService;

public abstract class LoaderSupport implements Loader 
{
    protected Logger log = Logger.getLogger(Loader.class.getName());
    int lineNumber = 1;
    
    @Inject 
    protected DbService db;

    public String getProjectDir()
    {
    	String dir = db.getString("SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='projects_dir'");
    	if( dir == null )
    		throw new NullPointerException(
                    "Project directory is not specified in systemsettings." + System.lineSeparator() +
                    "Please specify project directory in systemsettings, section_name=lims, setting_name=projects_dir. ");
    	
    	if( !dir.endsWith("/"))
    		dir = dir + "/";
    	
    	return dir;
    }

	public void checkFormat(File file) throws Exception
	{
    	try( BufferedReader br = new BufferedReader(new FileReader(file)) ) 
    	{
    	    doCheckFormat(file, br.readLine()); 
    	}
	}
    
	/**
	 * Checks file first line whether it corresponds to the file format.
	 *  
	 * @param firstLine
	 * @throws Exception
	 */
	protected void doCheckFormat(File file, String firstLine) throws Exception
	{
		if( getFormatFirstLine().equals(firstLine.trim()) )
			return;
		
		throw new Exception("First line in file " + file.getCanonicalPath() + " should be '" + getFormatFirstLine() + "'.");
	}

    protected void createTableFromTemplate(String template, String sample)
    {
        String tableName = template + sample;

        db.updateRaw("DROP TABLE IF EXISTS " + tableName);
        db.updateRaw("CREATE TABLE " + tableName  + " (LIKE " + template + " INCLUDING defaults INCLUDING constraints INCLUDING indexes)");

        db.updateRaw("DROP SEQUENCE IF EXISTS " + tableName  + "_seq");
        db.updateRaw("CREATE SEQUENCE " + tableName  + "_seq AS integer");
        db.updateRaw("ALTER TABLE " + tableName  + " ALTER COLUMN ID SET DEFAULT " + "nextval('" + tableName  + "_seq'::regclass)" );   
    }
	
	/**
	 * Returns first line for the corresponding file format.
	 */
	abstract public String getFormatFirstLine();

	
	/**
	 * Loads specified file that may correspond to specified sample in the project.
	 */
	public void load(File file, String project, String sample) throws IOException
	{
		preload(file, project, sample);
		
		try( BufferedReader br = new BufferedReader(new FileReader(file)) ) 
    	{
    	    String line;
    	    while( (line = br.readLine()) != null ) 
    	    {
    	    	processLine(line);
    	    	lineNumber++;
    	    }
    	}
    }

	/**
	 * Makes needed initializations, for example create the needed database table.
	 */
	protected void preload(File file, String project, String sample) {}

	/**
	 * Processes file content line by line.
	 */
    protected void processLine(String line)
    {
        try
        {
            if( line.startsWith("##") )
                processMeta(line);
            else if( line.startsWith("#") )
                processHeader(line);
            else
                processRow(line);
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, getFormat() + "v" + getVersion() +
                "parsing error: " + t + System.lineSeparator() +
                "line: " + lineNumber + System.lineSeparator() + line);
        }
    }

    protected void processMeta(String line)   throws Exception {}
    protected void processHeader(String line) throws Exception {}
    protected void processRow(String line)    throws Exception {}
	
    protected StringTokenizer processHeader(String line, String[] titles, int fieldsNumber) throws Exception  
    {
        // check titles
        StringTokenizer tokens = new StringTokenizer(line, "\t");
        
        for(int i=0; i<titles.length; i++)
        {
            if( ! tokens.hasMoreTokens() )
                throw new Exception("Incorrect header line, it should contains " + fieldsNumber + " fields.");
            
            String token = tokens.nextToken();
            if( !titles[i].equals(token) )
                throw new Exception("Incorrect header line, expected field " + titles[i] + ", but was " + token + ".");
        }

        return tokens;
    }
}
