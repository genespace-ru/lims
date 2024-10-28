package ru.biosoft.lims.loader;

import java.io.File;
import java.io.IOException;

/**
 * General interface to load (import) file content into corresponding database table.
 */
public interface Loader 
{
	/**
	 * Returns format name, for example VCF.
	 */
	public String getFormat();
	
	/**
	 * Returns format version, for example 4.2.
	 */
	public String getVersion();

	/**
	 * Checks file for suitability for this loader.
	 * 
	 * @param file - file to be loaded.
	 * @throws IOException - contains message why this file is not suitable.
	 */
	public void checkFormat(File file) throws Exception;

	/**
	 * Loads specified file that may correspond to specified sample in the project.
	 */
	public void load(File file, String project, String sample) throws IOException;
}
