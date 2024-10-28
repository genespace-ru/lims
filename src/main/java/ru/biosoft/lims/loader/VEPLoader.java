package ru.biosoft.lims.loader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.developmentontheedge.be5.database.QRec;

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

    
    /** Map of known SNV attributes loaded from the database. */
    protected Map<String, QRec>   attributesMap = new HashMap<>();    
    
    /** Set of all attribute names used in the loaded VEP file. */
    protected Set<String> attrSet = new TreeSet<>();         
    
    /** Set of all attribute names used in the loaded VEP file that are missing in the database. */
    protected Set<String> missingSet = new TreeSet<>();         
    
    /** Set of attribute names used in SNV features, they vary between records for the same SNV. */
    protected Set<String> featureSet = new TreeSet<>();         

    protected String snvTableName;
    protected String featureTableName;

    protected Map<String, String> snvValues; 
    protected Map<String, String> currentValues; 

    // current SNV
    protected String snvID;
    protected String chrom;
    protected int pos;
    
    protected void postload()
    {
        System.out.println("All attributes: " + attrSet);
        System.out.println("Missing in DB: "  + missingSet);
        System.out.println("Feature attributes: "  + featureSet);
    }

    protected void preload(File file, String project, String sample) 
    {
        snvTableName     = "snv_"          + sample;
        featureTableName = "snv_features_" + sample;

        Object result = db.one("SELECT tablename FROM pg_tables WHERE tablename=?", snvTableName); 
        if( result == null )
            throw new NullPointerException(
                    "SNV table '" + snvTableName + "' should alreade exists." + System.lineSeparator() +
                    "VEP data are linked to existing VCF data.");
            
        createTableFromTemplate("snv_features_", sample);
        
        preloadAttributes();
    }

    protected void preloadAttributes()
    {
        List<QRec> list = db.list("SELECT * FROM attributes WHERE entity='snv_' ");

        for(QRec rec: list)
        {
            String key   = rec.getString("title");
            attributesMap.put(key, rec);
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
    
        currentValues = new HashMap<>();
        for(int i=0; i<titles.length; i++)
        {
            String token = tokens.nextToken();
            if( !"-".equals(token) )
                currentValues.put(titles[i], token);
        }
        
        processExtra();
        
        String uv = "Uploaded_variation";
        if( snvValues == null ||  !snvValues.get(uv).equals(currentValues.get(uv)) )
        {
            snvValues = currentValues;

            parseLocation(currentValues);
            snvID = db.oneString("SELECT id FROM " + snvTableName + " WHERE vcf_chrom=? AND vcf_pos=?", chrom, pos);
            if( snvID == null )
                throw new NullPointerException("VEP file contains SNV " + currentValues.get("Uploaded_variation") + 
                                                "->" + chrom + "_" + pos + ", that is missing in SNV table " + snvTableName + ".");  
            
            insertSnv(Integer.parseInt(snvID));
        }

        insertFeature(snvID);
    }

    protected void processExtra()
    {
        String extra = currentValues.get("Extra");
        StringTokenizer tokens = new StringTokenizer(extra,";");
        
        while(tokens.hasMoreTokens())
        {
            String field = tokens.nextToken();
            int ind = field.indexOf('=');
            String key = field.substring(0, ind);
            String value = field.substring(ind+1);
            currentValues.put(key, value);
        }
        
        currentValues.remove("Extra");
    }
    
    protected void insertSnv(int snvID)
    {
        String json = db.oneString("SELECT attributes FROM " + snvTableName + " WHERE id=?", snvID);
        json = json.substring(0, json.length()-1); // remove } in the end
        
        json = json + buildJson(false) + "}";
        
        db.updateRaw("UPDATE " + snvTableName + " SET attributes=to_json(?::json) WHERE id=?", json, snvID);  
    }

    protected void insertFeature(String snvID)
    {
        if( snvID == null ) // SNV is not in VCF, skip it
            return;
        
        String json = "{" + buildJson(true).substring(1) + "}";
        
        db.updateRaw("INSERT INTO " + featureTableName + "(snv, feature, attributes) VALUES(?, ?, to_json(?::json))",
                      Integer.parseInt(snvID), currentValues.get("Feature_type"), json);  
    }
    
    protected String buildJson(boolean isFeature)
    {
        StringBuffer json = new StringBuffer();
        
        for(String key : currentValues.keySet() )
        {
            attrSet.add(key);
            
            QRec attr = attributesMap.get(key);
            String level = null;
            if( attr == null )
                missingSet.add(key);
            else
                level = attr.getString("level");
            
            // unknown attribute add both SNV and feature
            if( attr == null || level.equals("SNV") != isFeature )
            {
                json.append(", \"");
                json.append(key);
                json.append("\":\"");
                json.append(currentValues.get(key));
                json.append("\"");
            }
        }

        // compare snvValues and currentValues to find feature fields
        if( isFeature && snvValues != currentValues )
        {
            for(String key : currentValues.keySet() )
            {
                if( !snvValues.containsKey(key) || 
                    !snvValues.get(key).equals(currentValues.get(key)) )
                    featureSet.add(key);
            }
        }
        
        return json.toString();
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
    protected void parseLocation(Map<String, String> values)
    {
        String variation = values.get("Uploaded_variation"); 
        StringTokenizer tokens = new StringTokenizer(variation, "_/");
        
        chrom = tokens.nextToken();
        pos      = Integer.parseInt(tokens.nextToken());
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
    }

}