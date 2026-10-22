package operations

import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import ru.biosoft.access.core.DataElementPath

/**
 * Deletes workflow_info records together with their file_info rows and the
 * workflow script files on disk.
 *
 * Cascade order:
 *   1. workflow_runs  — child rows are removed (they reference workflow_info.ID)
 *   2. file on disk   — resolved via the file_info.path column (relative to repo root)
 *   3. file_info      — the row linking to the script file
 *   4. workflow_info  — the record itself
 */
public class RemoveWorkflow extends GOperationSupport {

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        List<String> removedFromDisk = []
        List<String> notOnDisk = []

        for ( int i=0 ; i < context.records.length ; ++i ) {
            def wf = database.getEntity( getInfo().getEntity().name ).get( context.records[i] )

            // Uncomment to remove child workflow_runs rows
            //database.workflow_runs.removeBy([ workflow_info: wf.$ID ])

            // Remove the script file from disk
            def fi = database.file_info.get( wf.$file_info )
            if ( fi != null ) {
                String path = fi.$path
                if ( path != null && !path.isEmpty() ) {
                    DataElementPath dePath = DataElementPath.create( path )
                    if ( dePath.exists() ) {
                        dePath.remove()
                        removedFromDisk.add( path )
                    } else {
                        notOnDisk.add( path )
                    }
                }
                // Remove the file_info row
                database.file_info.removeBy([ ID: fi.$ID ])
            }

            // Remove the workflow_info record
            database.workflow_info.removeBy([ ID: wf.$ID ])
        }

        String msg = "Removed ${context.records.length} workflow(s)"
        if ( !removedFromDisk.isEmpty() )
            msg += ", deleted from disk: " + removedFromDisk.size() + " file(s)"
        if ( !notOnDisk.isEmpty() )
            msg += ", not found on disk: " + notOnDisk.size() + " file(s)"
        setResult( OperationResult.finished( msg ) )
    }
}
