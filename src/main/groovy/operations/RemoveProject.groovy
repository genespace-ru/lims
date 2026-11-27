package operations

import javax.inject.Inject

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import ru.biosoft.access.core.DataElementPath
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.lims.util.SnvTables



public class RemoveProject extends GOperationSupport {
    Map<String, Object> presets

    @Inject
    private RepositoryManager repo;

    // Tables that have a column referencing projects
    // Order matters: children before parents to avoid dangling references mid-cascade.
    private static final List<String> REFERENCING_TABLES = [
        'task_runs',
        'workflow_runs',
        'qc_runs',
        'runs',
        'analyses',
        'reports',
        'file_info',
        'samples',
    ]

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        String repoPath = repo.getRepositoryPath();
        for ( int i=0 ; i < context.records.length ; ++i ) {
            def prj = database.getEntity( getInfo().getEntity().name ).get( context.records[i] )
            if(repoPath != null) {
                DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(prj.$name );
                if(projectPath.exists()) {
                    projectPath.remove()
                }
            }
            // Drop the per-sample snv_ / snv_meta_ tables before the samples are
            // removed (the tables are not collections, so they are not removed
            // automatically). file_info rows are removed by the REFERENCING_TABLES
            // cascade below (they reference projects). Collect the project's sample
            // IDs first.
            def sampleRows = db.listWithParams( "SELECT ID FROM samples WHERE project = ?", prj.$ID )
            for ( def row : sampleRows ) {
                SnvTables.dropForSample( db, row.$ID.toString() )
            }
            // Remove child records from all tables that reference projects.
            // Tables are not collections or genericCollection, so automatic removal is not performed
            for ( tableName in REFERENCING_TABLES ) {
                database.getEntity( tableName ).removeBy([ project: prj.$ID ])
            }
            database.projects.removeBy([ID: prj.$ID])
        }
        setResult(OperationResult.finished())
    }
}
