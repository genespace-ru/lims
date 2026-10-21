package operations

import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.access.DataCollectionUtils
import ru.biosoft.access.core.DataElementPath

import javax.inject.Inject
import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

public class CreateEmptyProject extends GOperationSupport {

    @Inject
    private RepositoryManager repo;

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        params.projectName = [value: presetValues.get("projectName"),
            DISPLAY_NAME: "Project name", TYPE: String]

        params.description = [value: "",
            DISPLAY_NAME: "Description", TYPE: String]

        params = DpsUtils.setValues(params, presetValues)

        def projectName = params.getValue("projectName")
        if (isProjectExists(projectName)) {
            validator.setError(params.projectName, "The project name you have entered already exists.\nPlease try again.")
            return params
        }

        return params
    }

    @Override
    public void invoke(Object parameters) throws Exception {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()
        def projectName = params.$projectName
        if (isProjectExists(projectName)) {
            setResult(OperationResult.error("The project name you have entered already exists."))
            return
        }

        String description = params.$description
        String repoPath = repo.getRepositoryPath()
        if (repoPath == null) {
            setResult(OperationResult.error("Repository is not available"))
            return
        }

        def projectId = database.projects << [name: projectName, description: description]

        DataElementPath parentPath = DataElementPath.create(repoPath)
        try {
            DataCollectionUtils.createSubCollection(parentPath.getChildPath(projectName))
        } catch (Exception e) {
            // Project row created but directory failed — clean up
            db.update("DELETE FROM projects WHERE ID = $projectId")
            setResult(OperationResult.error("Error creating project directory: " + e.getMessage()))
            return
        }

        setResult(OperationResult.finished())
    }

    def boolean isProjectExists(String projectName) {
        if (projectName == null) return false
        def projID = db.oneLong("SELECT id FROM projects prj WHERE name = ?", projectName)
        return (projID != null)
    }
}
