package ru.biosoft.lims.loader;

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VEPLoader links and loads VEP data snv_{sample} and snv_transcript_{sample} database tables.
 * 
 * https://www.ensembl.org/info/docs/tools/vep/vep_formats.html#defaultout
 */
public class VEPLoader extends LoaderSupport
{
    public static final Logger log = Logger.getLogger(VEPLoader.class.getName());

    public String getFormat()           { return "VEP"; }
    public String getVersion()          { return "112.0"; }
    public String getFormatFirstLine()  { return "## ENSEMBL VARIANT EFFECT PREDICTOR v112.0"; }

    protected String snvTableName;
    protected String transcriptTableName;
    
}