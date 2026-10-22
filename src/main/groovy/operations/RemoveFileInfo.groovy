package operations

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import ru.biosoft.access.core.DataElementPath

/**
 * Removes file_info records and deletes the corresponding files/folders on disk.
 *
 * The file_info.path column stores a path relative to the repository root
 * (e.g. projects/<name>/samples/<file>). We resolve it via DataElementPath
 * (which looks it up against the registered repository root) and call remove(),
 * which deletes the element and does nothing if it is already gone.
 */
public class RemoveFileInfo extends GOperationSupport {

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        List<String> removedFromDisk = []
        List<String> notOnDisk = []

        int childRowsRemoved = 0

        for ( int i=0 ; i < context.records.length ; ++i ) {
            def fi = database.getEntity( getInfo().getEntity().name ).get( context.records[i] )
            String path = fi.$path
            if ( path != null && !path.isEmpty() ) {
                DataElementPath dePath = DataElementPath.create( path )
                if ( dePath.exists() ) {
                    boolean isDirectory = dePath.getDataElement() instanceof ru.biosoft.access.core.DataCollection
                    dePath.remove()
                    removedFromDisk.add( path )
                    // Removing a directory deletes the whole subtree on disk, so any
                    // file_info rows whose path is nested under it become dangling —
                    // clear them too. The separator is appended so a sibling path that
                    // merely shares a prefix (e.g. "foo" vs "foobar") is not matched.
                    if ( isDirectory ) {
                        childRowsRemoved += db.updateRaw(
                            "DELETE FROM file_info WHERE path LIKE ? AND ID <> ?",
                            path.replace('\\', '/') + '/%', fi.$ID )
                    }
                } else {
                    notOnDisk.add( path )
                }
            }
            database.file_info.removeBy([ ID: fi.$ID ])
        }

        String msg = "Removed ${context.records.length} record(s)"
        if ( !removedFromDisk.isEmpty() )
            msg += ", deleted from disk: " + removedFromDisk.size() + " item(s)"
        if ( childRowsRemoved > 0 )
            msg += ", removed ${childRowsRemoved} nested file_info row(s)"
        if ( !notOnDisk.isEmpty() )
            msg += ", not found on disk: " + notOnDisk.size() + " item(s)"
        setResult( OperationResult.finished( msg ) )
    }
}
