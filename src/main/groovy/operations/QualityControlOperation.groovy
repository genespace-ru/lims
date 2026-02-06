package operations;

import javax.inject.Inject

import com.developmentontheedge.be5.server.operations.support.GOperationSupport;
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.beans.DynamicPropertySetSupport
import java.nio.file.Path
import ru.biosoft.access.core.DataElementPath
import ru.biosoft.access.file.GenericFileDataCollection
import ru.biosoft.access.DataCollectionUtils
import ru.biosoft.util.ApplicationUtils
import ru.biosoft.util.TempFiles
import ru.biosoft.lims.repository.RepositoryManager
import ru.biosoft.nextflow.NextflowService
import biouml.plugins.wdl.GeneSpaceContext
import biouml.plugins.wdl.NextFlowRunner
import ru.biosoft.nextflow.NextFlowRunnerLims

import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.be5.databasemodel.util.DpsUtils

public class QualityControlOperation extends GOperationSupport {

    @Inject
    private RepositoryManager repo;
    @Inject
    protected NextflowService nf;

    Map<String, Object> presets

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception {
        DPS params = new DynamicPropertySetSupport()
        presets = presetValues
        return null
    }

    @Override
    public void invoke(Object parameters) throws Exception {
        def workflowName = "fastqc.nf"
        def qPars = new groovy.json.JsonSlurper().parseText( presets._params_ )
        String repoPath = repo.getRepositoryPath();
        def prjId = qPars["___prjID"]
        def prj = database.getEntity( "projects" ).get( Long.parseLong(prjId ) )
        if(repoPath != null) {
            DataElementPath projectPath = DataElementPath.create(repoPath).getChildPath(prj.$name );
            GenericFileDataCollection prjDc = projectPath.getDataCollection();
            if(projectPath.exists()) {
                DataElementPath samples = projectPath.getChildPath("samples" );
                DataElementPath results = projectPath.getChildPath("results" );
                if(!results.exists()) {
                    DataCollectionUtils.createSubCollection(results);
                }
                GenericFileDataCollection resultsDc = results.getDataCollection();
                GenericFileDataCollection workflowsDc = repo.getWorkflowsCollection();
                def workflowQCPath = workflowsDc.getCompletePath().getChildPath(workflowName )
                def fileInfo = db.oneLong("SELECT id FROM file_info WHERE path = ?",  workflowQCPath.toString())
                if(fileInfo == null) {
                    setResult(OperationResult.error("Unknown file " + workflowQCPath.toString()))
                    return
                }
                def workflowInfo = db.oneLong( "SELECT id FROM workflow_info WHERE file_info = ?", fileInfo)
                if(workflowInfo == null) {
                    workflowInfo = database.workflow_info << [file_info: fileInfo, title: "Quality control", description: "Контроль качества fastq ридов."]
                }

                File samplesDir = prjDc.getFile("samples");
                File resultsDir = prjDc.getFile("results");

                Map<String, Object> nextflowParams = new HashMap<>();
                //params.readsDir   = "${params.projectDir}/samples"
                //params.fastqcDir  = "${params.projectDir}/results/fastqc"
                //params.multiqcDir = "${params.projectDir}/results/multiqc"
                nextflowParams.put("readsDir", samplesDir.getAbsolutePath() ) //absolute path on server same as on host if in docker
                nextflowParams.put("resultsDir", resultsDir.getAbsolutePath() ) //absolute path on server same as on host if in docker
                nextflowParams.put("fastqcDir", resultsDc.getFile("fastqc").getAbsolutePath() ) //absolute path on server same as on host if in docker
                nextflowParams.put("multiqcDir", resultsDc.getFile("multiqc").getAbsolutePath() ) //absolute path on server same as on host if in docker

                String nextFlowScript = ApplicationUtils.readAsString(workflowsDc.getFile(workflowName ));
                Path outputDir = TempFiles.getTempDirectory().toPath()

                def workflowRunId = nf.insertWorkflowRun ((int)prj.$id, (int)workflowInfo)

                def serverUrl = request.getServerUrl()


                String referer = null
                if( request != null && request.getRawRequest() != null ) {
                    referer = request.getRawRequest().getHeader( "referer" )
                }
                if(referer != null) {
                    serverUrl = referer.replaceAll('/$', '')
                }

                def serverDockerUrl = db.getString( "SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='docker_server_url'" );
                if( serverDockerUrl != null ) {
                    serverUrl = serverDockerUrl
                }

                def towerAddress = serverUrl+"/nf"

                boolean useDocker = false
                String useDockerStr = db.getString( "SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='docker_nextflow'" )
                if(useDockerStr != null) {
                    useDocker = Boolean.parseBoolean(useDockerStr )
                }

                nextflowParams.put("parseData", "\\\"prjId\\\":"+(int)prj.$id+",\\\"workflowId\\\":"+workflowRunId );
                nextflowParams.put("parseUrl", serverUrl+ "/nf/parse/multiqc" );

                GeneSpaceContext context = new GeneSpaceContext(repo.getProjectsPath(), repo.getWorkflowsPath(), repo.getGenomePath(), outputDir)
                NextFlowRunnerLims.runNextFlow(""+ workflowRunId, "fastqc", nextflowParams, nextFlowScript, false, useDocker, towerAddress, context)
            }
        }
    }
}
