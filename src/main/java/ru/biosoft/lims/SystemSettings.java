package ru.biosoft.lims;

import java.util.Hashtable;
import java.util.Map;

import javax.inject.Inject;
import com.developmentontheedge.be5.database.DbService;

public class SystemSettings 
{
	@Inject 
	protected static DbService db;

	protected static Map<String, String> valuesMap = new Hashtable<>();
	
	public static String getValue(String section, String setting, String title)
	{
		String key = section + "--" + setting;
		if( valuesMap.containsKey(key) )
			return valuesMap.get(key);
		
		String value = db.getString("SELECT setting_value FROM systemsettings WHERE section_name=? AND setting_name=?", section, setting);
		if( value == null )
			throw new NullPointerException(
	                "Setting " + title + " is not specified in systemsettings." + System.lineSeparator() +
	                "Please specify its, section_name=" + section + ", setting_name=" + setting + ".");
		
		valuesMap.put(key, value);
		return value;
	}
	
	public static String getDirectory(String section, String setting, String title, boolean asLinux)
	{
		String dir = getValue(section, setting, title);  
		if( !dir.endsWith("/"))
			dir = dir + "/";
	
		if( asLinux )
			dir = SystemSettings.asLinux(dir);

		return dir;
	}

	public static String asLinux(String dir)
	{
		dir = dir.replaceFirst("C:", "/mnt/c");
		
		return dir;
	}
	
	public static String getDataDir(boolean asLinux)
	{
		return getDirectory("lims", "data_dir", "data directory", asLinux);
	}

	
	public static String getProjectsDir(boolean asLinux)
	{
		return getDirectory("lims", "projects_dir", "project directory", asLinux);
	}

	public static String getNextflowDir(boolean asLinux)
	{
		return getDirectory("lims", "nextflow_dir", "project directory", asLinux);
	}
	
	public static String getWorkflowRunsDir()
	{
		return getDirectory("lims", "workflow_runs_dir", "project directory", false);
	}
	
	public static String getNextflowTraceAPI()
	{
		return getValue("lims", "nextflow-trace-api", "Nextflow trace API");
	}
}
