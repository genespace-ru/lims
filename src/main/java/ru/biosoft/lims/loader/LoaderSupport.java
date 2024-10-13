package ru.biosoft.lims.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.inject.Inject;

import com.developmentontheedge.be5.database.DbService;

public abstract class LoaderSupport implements Loader 
{
    @Inject 
    private DbService db;

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

	/**
	 * Returns first line for the corresponding file format.
	 */
	abstract public String getFormatFirstLine();

	
	/**
	 * Loads specified file that may correspond to specified sample in the project.
	 */
	public void load(File file, String project, String sample) throws IOException
	{
    	try( BufferedReader br = new BufferedReader(new FileReader(file)) ) 
    	{
    	    String line;
    	    while( (line = br.readLine()) != null ) 
    	    {
    	    	processLine(line);
    	    }
    	}
    }

	/**
	 * Processes file content line by line.
	 */
	abstract protected void processLine(String line);
}
