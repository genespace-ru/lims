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
    }

    /**
     * The default output format ("VEP" format when downloading from the web interface) is a 14 column tab-delimited file. Empty values are denoted by '-'. 
     * 
     * The output columns are:
     *
     * <ol>
     * <li> Uploaded variation - as chromosome_start_alleles
     * <li> Location - in standard coordinate format (chr:start or chr:start-end)
     * <li> Allele - the variant allele used to calculate the consequence
     * <li> Gene - Ensembl stable ID of affected gene
     * <li> Feature - Ensembl stable ID of feature
     * <li> Feature type - type of feature. Currently one of Transcript, RegulatoryFeature, MotifFeature.
     * <li> Consequence - consequence type of this variant
     * <li> Position in cDNA - relative position of base pair in cDNA sequence
     * <li> Position in CDS - relative position of base pair in coding sequence
     * <li> Position in protein - relative position of amino acid in protein
     * <li> Amino acid change - only given if the variant affects the protein-coding sequence
     * <li> Codon change - the alternative codons with the variant base in upper case
     * <li> Co-located variation - identifier of any existing variants. Switch on with --check_existing
     * <li> Extra - this column contains extra information as key=value pairs separated by ";", see below.
     * </li>
     *
     * Example:
     * #Uploaded_variation Location    Allele  Gene    Feature Feature_type    Consequence cDNA_position   CDS_position    Protein_position    Amino_acids Codons  Existing_variation  Extra
     * 
     */
    public static final String[] titles = new String[] {"#Uploaded_variation", "Location", "Allele", "Gene", "Feature", "Feature_type", "Consequence",
            "cDNA_position", "CDS_position", "Protein_position", "Amino_acids", "Codons", "Existing_variation", "Extra"};

    protected void processHeader(String line) throws Exception  
    {
        processHeader(line, titles, 14);
    }
    
}