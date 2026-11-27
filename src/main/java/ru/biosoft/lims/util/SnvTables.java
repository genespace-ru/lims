package ru.biosoft.lims.util;

import com.developmentontheedge.be5.database.DbService;

/**
 * Helpers for the per-sample {@code snv_<sampleId>} / {@code snv_meta_<sampleId>}
 * tables that {@code VCFLoader} creates when importing a VCF result.
 *
 * <p>These tables are plain (non-collection) database tables named after the
 * {@code samples.ID} of the sample the VCF was imported for. Because there is
 * one table per sample (each import drops and recreates it) and a sample can be
 * populated by several runs, the tables belong to the sample/project, not to a
 * workflow run. They are therefore dropped when the sample or the project is
 * removed — never when a single run is removed.
 */
public class SnvTables
{
    private SnvTables()
    {
    }

    /**
     * Drops {@code snv_meta_<sampleId>} and {@code snv_<sampleId>} if they exist.
     * Each drop is guarded by a {@code pg_class} existence check, so a sample
     * that never had a VCF loaded (or whose tables were already removed) is a
     * no-op.
     *
     * @return the number of tables actually dropped (0, 1, or 2).
     */
    public static int dropForSample(DbService db, String sampleId)
    {
        int dropped = 0;
        for( String table : new String[] { "snv_meta_" + sampleId, "snv_" + sampleId } )
        {
            if( tableExists( db, table ) )
            {
                db.updateRaw( "DROP TABLE " + table );
                dropped++;
            }
        }
        return dropped;
    }

    /** Whether a relation with the given name exists in the database. */
    public static boolean tableExists(DbService db, String tableName)
    {
        try
        {
            com.developmentontheedge.be5.database.QRec row = db.recordWithParams( "SELECT EXISTS(SELECT 1 FROM pg_class WHERE relname = ?) AS \"exists\"", tableName );
            if( row == null )
                return false;
            String v = row.getString( "exists" );
            return v != null && ( v.equalsIgnoreCase( "true" ) || v.equals( "1" ) );
        }
        catch( Exception e )
        {
            return false;
        }
    }
}
