package operations

import javax.inject.Inject

import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import ru.biosoft.access.core.DataElementPath
import ru.biosoft.lims.repository.RepositoryManager

/**
 * Deletes workflow_runs records together with their task_runs rows, the run's
 * results folder on disk, and file_info rows pointing at its results.
 *
 * Attached to the workflow_runs "ForProjectCard" query, whose entity primary
 * key is the run ID itself — so context.records[i] is the ID of the run to
 * delete (the visible "ID" column shows the same value).
 *
 * The results folder is resolved via workflow_runs.comment, which
 * RunWorkflowOperation sets to the run's subfolder name ("workflow_run_<id>")
 * under the project's results directory. Removing the folder deletes the whole
 * subtree on disk, so no per-file removal is needed — file_info registration
 * only controls UI visibility, the folder is the source of truth. Runs without
 * a results folder (e.g. QualityControl runs that publish directly into the
 * results subdirectories) have a null comment and are handled accordingly.
 *
 * Cascade order (children before parents; the tables are not data collections,
 * so no automatic removal happens — same approach as RemoveProject):
 *   1. task_runs    — child rows (task_runs.workflow -> workflow_runs.ID)
 *   2. results folder on disk — projects/<name>/results/<comment>
 *   3. file_info    — rows with entity='workflow_runs', entityID=run ID
 *   4. workflow_runs — the record itself
 */
public class RemoveWorkflowRun extends GOperationSupport {

    @Inject
    private RepositoryManager repo;

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        String repoPath = repo.getRepositoryPath();

        List<String> removedFromDisk = []
        List<String> noFolder = []
        int runsRemoved = 0
        int tasksRemoved = 0
        int fileInfoRowsRemoved = 0

        for ( int i=0 ; i < context.records.length ; ++i ) {
            Long runId = context.records[i]
            def run = database.workflow_runs.get( runId )
            if ( run == null ) {
                // Stale record ID (e.g. the tab was not reloaded after a previous delete)
                continue
            }

            // 1. Remove child task_runs rows
            tasksRemoved += db.updateRaw( "DELETE FROM task_runs WHERE workflow = ?", runId )

            // 2. Remove the run results folder on disk (projects/<name>/results/<comment>)
            String runFolderName = run.$comment
            def prj = database.projects.get( run.$project )
            if ( runFolderName != null && !runFolderName.isEmpty() && prj != null && repoPath != null ) {
                DataElementPath runFolder = DataElementPath.create( repoPath )
                        .getChildPath( prj.$name )
                        .getChildPath( "results" )
                        .getChildPath( runFolderName )
                if ( runFolder.exists() ) {
                    runFolder.remove()
                    removedFromDisk.add( prj.$name + "/results/" + runFolderName )
                } else {
                    noFolder.add( runFolderName )
                }
            }

            // 3. Remove file_info rows registered for the run's results
            fileInfoRowsRemoved += db.updateRaw( "DELETE FROM file_info WHERE entity='workflow_runs' AND entityID = ?", runId )

            // 4. Remove the workflow_runs record
            database.workflow_runs.removeBy([ ID: runId ])

            runsRemoved++
        }

        String msg = "Removed ${runsRemoved} workflow run(s)"
        if ( tasksRemoved > 0 )
            msg += ", ${tasksRemoved} task run(s)"
        if ( fileInfoRowsRemoved > 0 )
            msg += ", ${fileInfoRowsRemoved} file_info row(s)"
        if ( !removedFromDisk.isEmpty() )
            msg += ", deleted from disk: " + removedFromDisk.size() + " folder(s)"
        if ( !noFolder.isEmpty() )
            msg += ", folder not found on disk: " + noFolder.size()
        setResult( OperationResult.finished( msg ) )
    }
}
