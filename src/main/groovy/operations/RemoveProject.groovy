package operations

import javax.inject.Inject

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import ru.biosoft.access.core.DataElementPath
import ru.biosoft.lims.repository.RepositoryManager



public class RemoveProject extends GOperationSupport {
    Map<String, Object> presets

    @Inject
    private RepositoryManager repo;

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
            database.projects.removeBy([ID: prj.$ID])
        }
        setResult(OperationResult.finished())
    }
}
