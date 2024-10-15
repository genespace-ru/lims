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
    
    protected void preload(File file, String project, String sample) 
    {
        snvTableName        = "snv_"      + sample;
        transcriptTableName = "snv_transcripts_" + sample;

        Object result = db.one("SELECT tablename FROM pg_tables WHERE tablename=?", snvTableName); 
        if( result == null )
            throw new NullPointerException(
                    "SNV table '" + snvTableName + "' should alreade exists." + System.lineSeparator() +
                    "VEP data are linked to existing VCF data.");
            
        createTableFromTemplate("snv_transcripts_", transcriptTableName);
    
        System.out.println("ok");
    }
    
    protected void processLine(String line)
    {}
    
}