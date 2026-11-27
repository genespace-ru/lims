package operations

import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

/**
 * Delete operation that nulls out references in child tables before deleting.
 *
 * Configure via presets in the YAML operation definition:
 *   presets:
 *     children:
 *       - table: qc_runs
 *         column: sample
 *       - table: task_runs
 *         column: sample
 *       - table: reports
 *         column: sample_id
 *
 * Before deleting each record, all rows in the listed child tables where
 * the column matches the deleted ID get that column set to NULL.
 */
public class RemoveWithNullChildren extends GOperationSupport {
    Map<String, Object> presets

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        def entityName = getInfo().getEntity().name
        def children = presets?.children ?: []

        for ( int i=0 ; i < context.records.length ; ++i ) {
            long id = context.records[i]

            for ( child in children ) {
                database.getEntity( child.table ).setBy([ (child.column): null ], [ (child.column): id ])
            }

            database.getEntity( entityName ).removeBy([ ID: id ])
        }
        setResult(OperationResult.finished())
    }
}
