package ru.biosoft.lims.loader;

/**
 * VCFLoader loads SNV from VCFv4.2 file into corresponding database table.
 */
public class VCFLoader extends LoaderSupport
{
	public String getFormat()			{ return "VCF"; }
	public String getVersion()			{ return "4.2"; }
	public String getFormatFirstLine()	{ return "##fileformat=VCFv4.2"; }

	protected void processLine(String line)
	{}
	
}