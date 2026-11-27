package extenders

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.OperationExtenderSupport
import ru.biosoft.lims.util.SnvTables

class EntityEditDeleteExtender extends OperationExtenderSupport
{
    @Override
    Object postGetParameters(Operation op, Object parameters, Map<String, Object> presetValues) throws Exception
    {
        if( userInfo.isAdmin() || userInfo.isUserInRole( "Manager" ) )
            return parameters

        for ( int i=0 ; i < op.context.records.length ; ++i )
        {
            def dps = database.getEntity( op.getInfo().getEntity().name ).get(op.context.records[i])
            if( userInfo.userName != dps.$whoInserted___ && userInfo.userName != dps.$whoModified___ )
            {
                op.setResult( OperationResult.error( "Для пользователей допускается только редактирование и удаление записей, созданных самим пользователем" ) )
            }
        }
        return parameters
    }

    /**
     * Runs before the standard delete operation removes the records. When a
     * sample is deleted, clean up the things that reference it but are not
     * automatically removed:
     * <ul>
     *   <li>the per-sample {@code snv_<id>} / {@code snv_meta_<id>} tables
     *       (created by VCF import; plain tables, not collections) — the drop is
     *       guarded by a {@code pg_class} existence check, so a sample that never
     *       had a VCF loaded is a no-op;</li>
     *   <li>the {@code file_info} rows attached to the sample
     *       ({@code entity='samples'}); the link is a soft (non-FK) one, so the
     *       standard delete does not cascade them.</li>
     * </ul>
     */
    @Override
    void preInvoke(Operation op, Object parameters) throws Exception
    {
        if( !"samples".equals( op.getInfo().getEntity().name ) )
            return

        for ( int i=0 ; i < op.context.records.length ; ++i )
        {
            String sampleId = op.context.records[i].toString()
            SnvTables.dropForSample( db, sampleId )
            db.updateRaw( "DELETE FROM file_info WHERE entity = 'samples' AND entityID = ?", sampleId )
        }
    }
}