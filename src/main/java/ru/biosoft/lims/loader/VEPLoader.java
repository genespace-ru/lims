package ru.biosoft.lims.loader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.query.services.QueriesService;

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
    protected Map<String, QRec> snvAttributes = new HashMap();    
    protected Map<String, QRec> transcriptAttributes = new HashMap();
    protected Map<String, String> transcriptDiff = new HashMap();

    protected Map<String, String> snvValues; 
    protected Map<String, String> currentValues; 
   
    protected final int STATE_NEW_SNV = 1; 
    protected int state = STATE_NEW_SNV;
    
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
        
        preloadAttributes();
    }

    protected void preloadAttributes()
    {
        List<QRec> list = db.list("SELECT * FROM attributes WHERE entity='snv_' ");

        for(QRec rec: list)
        {
            String key   = rec.getString("title");
            String level = rec.getString("level");
            
            if( "SNV".equals(level) )
                snvAttributes.put(key, rec);
            else if( "transcript".equals(level) )
                transcriptAttributes.put(key, rec);
        }
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
    public static final String[] titles = new String[] {"Uploaded_variation", "Location", "Allele", "Gene", "Feature", "Feature_type", "Consequence",
            "cDNA_position", "CDS_position", "Protein_position", "Amino_acids", "Codons", "Existing_variation", "Extra"};

    protected void processHeader(String line) throws Exception  
    {
        processHeader(line, titles, 14);
    }

    /**
     * Example:
     * 
     * <code>
     * #Uploaded_variation  Location                 Allele  Gene            Feature         Feature_type    Consequence                                         
     * chr10_87960877_-/T   chr10:87960876-87960877  T       ENSG00000171862 ENST00000371953 Transcript      splice_polypyrimidine_tract_variant,intron_variant
     * 
     * cDNA_position  CDS_position  Protein_position  Amino_acids Codons  Existing_variation  Extra
     * -              -             -                 -           -       rs34003473          IMPACT=LOW;STRAND=1;VARIANT_CLASS=insertion;SYMBOL=PTEN, ...
     * </code>
     * */
    protected void processRow(String line) throws Exception 
    {
        StringTokenizer tokens = new StringTokenizer(line, "\t");
    
        currentValues = new HashMap();
        for(int i=0; i<titles.length; i++)
        {
            String token = tokens.nextToken();
            if( !"-".equals(token) )
                currentValues.put(titles[i], token);
        }
        
        int pos = getLocation(currentValues);
        String chrom = currentValues.get("chrom");
        
        String snvID = db.oneString("SELECT id FROM " + snvTableName + " WHERE vcf_chrom=? AND vcf_pos=?", chrom, pos);
        if( snvID == null )
            throw new NullPointerException("VEP file contains SNV " + currentValues.get("UPLOADED_VARIATION") + 
                                            "->" + chrom + "_" + pos + ", that is missing in SNV table " + snvTableName + ".");  
    }

    /*
     * VCF
     * #CHROM  POS         REF   ALT
     * chr10   87961150    T     G
     * chr10   87960876    C     CT
     * chr11   108227732   TAA   T
     * 
     * VEP
     * #Uploaded_variation   Location                    Allele
     * chr10_87961150_T/G    chr10:87961150              G
     * chr10_87960877_-/T    chr10:87960876-87960877     T
     * chr11_108227733_AA/-  chr11:108227733-108227734   
     */    
    protected int getLocation(Map<String, String> values)
    {
        String variation = values.get("Uploaded_variation"); 
        StringTokenizer tokens = new StringTokenizer(variation, "_/");
        
        String chrom = tokens.nextToken();
        int pos      = Integer.parseInt(tokens.nextToken());
        String ref   = tokens.nextToken();
        String alt   = tokens.nextToken();
        
        for(int i=0; i<ref.length(); i++)
        {
            if( ref.charAt(i) == '-' )
                pos--;
        }
        
        for(int i=0; i<alt.length(); i++)
        {
            if( alt.charAt(i) == '-' )
                pos--;
        }
        
        values.put("chrom",    chrom);
        
        return pos;
    }
}