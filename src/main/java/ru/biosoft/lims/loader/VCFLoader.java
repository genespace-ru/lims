package ru.biosoft.lims.loader;

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.developmentontheedge.be5.database.DbService;

/**
 * VCFLoader loads SNV from VCFv4.2 file into corresponding database table.
 * 
 * Two tables are created:
 * 1) vcf_<sample_name> (snv_ table is used as template) - stores SNV data
 * 2) vcf_meta_<sample_name> (snv_memta_ table is used as template) - stores metadata
 *
 * Limitations:
 * 
 * 1) Only one sample is supported.
 * 
 * 2) Not parsed meta for fields:
 * 
 * 1.2.8 Sample field format
 * ##SAMPLE=<ID=S_ID,Genomes=G1_ID;G2_ID; ...;GK_ID,Mixture=N1;N2; ...;NK,Description=S1;S2; ...;SK>
 * 
 * 1.2.9 Pedigree field format
 * ##PEDIGREE=<Name_0=G0-ID,Name_1=G1-ID,...,Name_N=GN-ID>
 * or a link to a database:
 * ##pedigreeDB=URL
 * 
 * 3) INFO fields is not parsed.
 * 
 * Comments in code refer to VCF specification
 * https://samtools.github.io/hts-specs/VCFv4.2.pdf
 */
public class VCFLoader extends LoaderSupport
{
    public static final Logger log = Logger.getLogger(VCFLoader.class.getName());

    public String getFormat()           { return "VCF"; }
    public String getVersion()          { return "4.2"; }
    public String getFormatFirstLine()  { return "##fileformat=VCFv4.2"; }

    protected String snvTableName;
    protected String metaTableName;

    /**
     * Manually assigns the injected DbService (VCFLoader has an implicit
     * no-arg constructor, so a plain {@code new VCFLoader()} does not get
     * field injection; callers outside Guice should set this first).
     */
    public void setDb(DbService db)
    {
        this.db = db;
    }

    protected void preload(File file, String project, String sample)
    {
        snvTableName  = "snv_"      + sample;
        metaTableName = "snv_meta_" + sample;

        createTableFromTemplate("snv_", sample);
        createTableFromTemplate("snv_meta_", sample);
    }

    protected void insertMeta(String section, String value)
    {
        db.updateRaw("INSERT INTO " + metaTableName + "(section, value) VALUES(?, ?)", section, value);
    }
    
    /**
     * Returns fieldName.
     * Expected format:
     * <code>##fieldName=...</code>
     */
    protected String getField(String line) throws Exception
    {
        int end = line.indexOf('=', 2);
        if( end == -1 )
            throw new Exception("Field format error, '=' is missing.");
        
        return line.substring(2, end);
    }
    
    /**
     * Returns key value.
     * Expected format:
     * <code><[...,]key=["]value["][,...]>
     * ##fieldName=...</code>
     */
    protected String getValue(String line, String key, boolean optional) throws Exception
    {
        int start = line.indexOf(key+'=');
        if( start == -1 )
        {
            if ( ! optional)
                throw new Exception("Field format error, key=" + key +" is missing.");
            else
                return "null";
        }
        
        start += key.length() + 1;
        int end;
        if( line.charAt(start) == '"' )
        {
            start++;
            // start now points just after the opening quote; find the closing
            // quote (which is at `start` for an empty value "").
            end = line.indexOf('"', start);
        }
        else
        {
            end = line.indexOf(',', start+1);
            if( end == -1 )
                end = line.indexOf('>', start+1);
        }
        if( end == -1 )
            throw new Exception("Field end undefined, key=" + key +".");

        return line.substring(start, end);
    }
        
    protected void processMeta(String line) throws Exception
    {
        String field = getField(line);
        String value = line.substring(3+field.length()).trim(); 
        
        // 1.2.1 File format
        if( "fileformat".equalsIgnoreCase(field) )
        {
            insertMeta(field, value);
            return;
        }

        // 1.2.2 Information field format
        // INFO fields should be described as follows (first four keys are required, source and version are recommended):
        // ##INFO=<ID=ID,Number=number,Type=type,Description="description",Source="source",Version="version">
        if( "INFO".equalsIgnoreCase(field) )
        {
            String id          = getValue(value, "ID",     false); 
            String number      = getValue(value, "Number", false); 
            String type        = getValue(value, "Type",   false); 
            String description = getValue(value, "Description", false); 

            db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, number, type, description) " +
                          "VALUES('INFO', ?, ?, ?, ?, ?)", value, id, number, type, description);
            return;
        }
        
        // 1.2.3 Filter field format
        // ##FILTER=<ID=ID,Description="description">       
        if( "FILTER".equalsIgnoreCase(field) )
        {
            String id = getValue(value, "ID", false); 
            String description = getValue(value, "Description", false); 

            db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, description) VALUES('FILTER', ?, ?, ?)", value, id, description);
            return;
        }

        // 1.2.4 Individual format field format
        // ##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
        // fields specified in the FORMAT field should be described as follows:
        // ##FORMAT=<ID=ID,Number=number,Type=type,Description="description">
        // Possible Types for FORMAT fields are: Integer, Float, Character, and String 
        if( "FORMAT".equalsIgnoreCase(field) )
        {
            String id          = getValue(value, "ID",     false); 
            String number      = getValue(value, "Number", false); 
            String type        = getValue(value, "Type",   false); 
            String description = getValue(value, "Description", false); 

            db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, number, type, description) " +
                          "VALUES('FORMAT', ?, ?, ?, ?, ?)", value, id, number, type, description);
            return;
        }
        
        // 1.2.5 Alternative allele field format
        // Symbolic alternate alleles for imprecise structural variants:
        // ##ALT=<ID=type,Description="description">
        if( "ALT".equalsIgnoreCase(field) )
        {
            String id = getValue(value, "ID", false); 
            String description = getValue(value, "Description", false); 

            db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, description) VALUES('ALT', ?, ?, ?)", value, id, description);
            return;
        }

        // 1.2.6 Assembly field format
        // Breakpoint assemblies for structural variations may use an external file:
        // ##assembly=url
        if( "assembly".equalsIgnoreCase(field) )
        {
            insertMeta(field, value);
            return;
        }
        
        // 1.2.7 Contig field format
        // ##contig=<ID=ctg1,URL=ftp://somewhere.org/assembly.fa,...>       
        if( "contig".equalsIgnoreCase(field) )
        {
            String id = getValue(value, "ID", false); 
            String url = getValue(value, "URL", true); 
            
            db.updateRaw("INSERT INTO " + metaTableName + "(section, value, id, url) " +
                          "VALUES('contig', ?, ?, ?)", value, id, url);
            return;
        }

        // other tags:
        insertMeta(field, value);
    }
    
    /*
     * 1.3 Header line syntax
     * The header line names the 8 fixed, mandatory columns. These columns are as follows:
     * 1. #CHROM
     * 2. POS
     * 3. ID
     * 4. REF
     * 5. ALT
     * 6. QUAL
     * 7. FILTER
     * 8. INFO
     * 
     * The header line is tab-delimited.
     * 
     * If genotype data is present in the file, these are followed by a FORMAT column header, then an arbitrary number
     * of sample IDs. 
     * 
     * Limitations: only one sample is supported!
     * 
     * Example:
     * #CHROM   POS        ID   REF ALT QUAL    FILTER  INFO    FORMAT              default
     * chr1     46258772    .   GA  G   0       RefCall .       GT:GQ:DP:AD:VAF:PL  0/0:35:15:13,2:0.133333:0,35,55
     */
    public static final String[] titles = new String[] {"CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO", "FORMAT"};
    /**
     * The sample column names from the header, in order. The first sample (e.g.
     * "TUMOR" in a somatic tumor/normal VCF) is the one whose genotype/attributes
     * are stored in the snv_ table; any additional sample columns are ignored.
     */
    protected java.util.List<String> sampleNames = new java.util.ArrayList<String>();
    protected void processHeader(String line) throws Exception
    {
        StringTokenizer tokens = processHeader(line, titles, 9);

        if( ! tokens.hasMoreTokens() )
            throw new Exception("Incorrect header line, it should contain at least 1 sample, 9 fields.");
        // One or more sample columns are allowed (e.g. somatic VCFs carry TUMOR
        // and NORMAL). The first is used; the rest are accepted but not stored.
        while( tokens.hasMoreTokens() )
            sampleNames.add( tokens.nextToken() );
    }
    
    protected void processRow(String line) throws Exception     
    {
        StringTokenizer tokens = new StringTokenizer(line, "\t");
        
        String chrom = tokens.nextToken();
        String pos   = tokens.nextToken();
        String id    = tokens.nextToken();
        String ref   = tokens.nextToken();
        String alt   = tokens.nextToken();
        String qual  = tokens.nextToken();
        String filter= tokens.nextToken();
        String info  = tokens.nextToken();
        String format= tokens.nextToken();
        String sample= tokens.nextToken();

        String json = buildJson(format, sample);

        // The VCF missing-value token "." means "no value" for ID and QUAL.
        // QUAL is numeric (crashes parse otherwise); ID is a nullable string.
        // (FILTER is NOT NULL in the schema, so its value is stored as-is.)
        String idValue = ".".equals(id) ? null : id;
        Double qualValue = ".".equals(qual) ? null : Double.valueOf(qual);

        String sql = "INSERT INTO " + snvTableName +
                     "(vcf_chrom, vcf_pos, vcf_id, vcf_ref, vcf_alt, vcf_qual, vcf_filter, " +
                     " vcf_info, vcf_format, attributes) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, to_json(?::json) )";
        try
        {
            db.updateRaw(sql, chrom, Integer.parseInt(pos), idValue, ref, alt, qualValue, filter, info, format, json);
        }
        catch( Exception e )
        {
            // Attach the failing SQL (with the row values) so verbose logging can show it.
            throw new Exception("SQL failed: " + sql + System.lineSeparator() +
                    "  args: [" + chrom + ", " + pos + ", " + idValue + ", " + ref + ", " + alt +
                    ", " + qualValue + ", " + filter + ", " + info + ", " + format + ", " + json + "]", e);
        }
    }

    protected String buildJson(String format, String sample)
    {
        StringTokenizer keys   = new StringTokenizer(format, ":");
        StringTokenizer values = new StringTokenizer(sample, ":");
        
        StringBuffer json = new StringBuffer("{");
        
        while( keys.hasMoreTokens() )
        {
            json.append("\"");
            json.append(keys.nextToken());
            json.append("\":\"");
            json.append(values.nextToken());
            
            if( keys.hasMoreTokens() )
                json.append("\", ");
            else
                json.append("\" }");
        }
        
        return json.toString();
    }
}