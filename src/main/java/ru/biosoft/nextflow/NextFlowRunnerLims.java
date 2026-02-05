package ru.biosoft.nextflow;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import biouml.plugins.wdl.GeneSpaceContext;
import biouml.plugins.wdl.NextFlowRunner;
import one.util.streamex.StreamEx;
import ru.biosoft.util.ApplicationUtils;

/*
 * Temporary class for faster development
 */
public class NextFlowRunnerLims extends NextFlowRunner
{

    private static final Logger log = Logger.getLogger( NextFlowRunnerLims.class.getName() );
    public static void runNextFlow(String id, String name, Map<String, Object> parameters, String nextFlowScript, boolean useWsl, boolean useDocker, String towerAddress,
            GeneSpaceContext context) throws Exception
    {
        File outputDir = context.getOutputDir().toFile();
        outputDir.mkdirs();
        File config = generateConfig( name, parameters, outputDir, useDocker, context );

        File f = new File( outputDir, name + ".nf" );
        ApplicationUtils.writeString( f, nextFlowScript );

        ProcessBuilder pb = null;
        if( useDocker )
            pb = getNextflowDockerProcessBuilder( f.getName(), config.getName(), id, towerAddress, context );
        else
            pb = getNextflowLocalProcessBuilder( f.getName(), config.getName(), id, towerAddress, context, useWsl );

        log.log( Level.INFO, "COMMAND: " + StreamEx.of( pb.command() ).joining( " " ) );
        System.out.println( "COMMAND: " + StreamEx.of( pb.command() ).joining( " " ) );
        Process process = pb.start();
        executeProcess( process );
    }

    /*
     * Run nextflow as local process in Windows or Unix
     */
    private static ProcessBuilder getNextflowLocalProcessBuilder(String nextFlowScriptName, String nextFlowConfig, String id, String towerAddress, GeneSpaceContext context,
            boolean useWsl)
    {

        String parent = context.getOutputDir().toAbsolutePath().toString().replace( "\\", "/" );
        String command = "export TOWER_WORKFLOW_ID=" + id + " ; export TOWER_ACCESS_TOKEN=zzz ; nextflow " + nextFlowScriptName + " -c " + nextFlowConfig;
        if( towerAddress != null )
            command += " -with-tower \'" + towerAddress + "\'";

        List<String> baseCommand;

        if( useWsl )
            baseCommand = List.of( "wsl", "--cd", parent, "bash", "-c", command );
        else
            baseCommand = List.of( "bash", "-c", "cd " + parent + " && " + command );

        ProcessBuilder pb = new ProcessBuilder( baseCommand );
        if( !useWsl )
            pb.directory( context.getOutputDir().toFile() );
        return pb;

    }

    /*
     * Run nextflow as docker container in Unix. All paths should be mapped into container as SAME paths. Otherwise if
     * nextflow script contains inner docker tools, they will not see the paths.
     * 4 folders with user projects, workflows, supporting data and working folder (can be temporary) are mapped into container. 
     * All inputs/results/used items should be descendants of one of the 4 folders above. 
     */
    private static ProcessBuilder getNextflowDockerProcessBuilder(String nextFlowScriptName, String nextFlowConfig, String id, String towerAddress, GeneSpaceContext context)
    {

        String containerName = "nf-" + id;
        String workDir = context.getOutputDir().toString();//"/nf-work";

        List<String> cmd = new ArrayList<>();
        cmd.add( "docker" );
        cmd.add( "run" );
        cmd.add( "--rm" );
        cmd.add( "--name" );
        cmd.add( containerName );

        cmd.add( "-w" );
        cmd.add( context.getOutputDir().toString() );
        if( towerAddress != null )
        {
            cmd.add( "-e" );
            cmd.add( "TOWER_WORKFLOW_ID=" + id );
            cmd.add( "-e" );
            cmd.add( "TOWER_ACCESS_TOKEN=zzz" );

        }

        cmd.add( "-v" );
        cmd.add( dockerVolume( Paths.get( "/var/run/docker.sock" ), "/var/run/docker.sock" ) );

        if( context.getProjectsDir() != null )
        {
            cmd.add( "-v" );
            cmd.add( dockerVolume( context.getProjectsDir(), context.getProjectsDir().toString() ) );
        }

        if( context.getGenomeDir() != null )
        {
            cmd.add( "-v" );
            cmd.add( dockerVolume( context.getGenomeDir(), context.getGenomeDir().toString() ) );
        }

        if( context.getWorkflowsDir() != null )
        {
            cmd.add( "-v" );
            cmd.add( dockerVolume( context.getWorkflowsDir(), context.getWorkflowsDir().toString() ) );
        }

        if( context.getOutputDir() != null )
        {
            cmd.add( "-v" );
            cmd.add( dockerVolume( context.getOutputDir(), context.getOutputDir().toString() ) );
        }

        cmd.add( "nextflow/nextflow:25.10.3" );
        cmd.add( "nextflow" );
        cmd.add( "run" );
        cmd.add( nextFlowScriptName );
        cmd.add( "-c" );
        cmd.add( workDir + "/" + nextFlowConfig );

        if( towerAddress != null )
        {
            cmd.add( "-with-tower" );
            cmd.add( "\'" + towerAddress + "\'" );
        }

        ProcessBuilder pb = new ProcessBuilder( cmd );

        return pb;

    }

    static String dockerVolume(Path host, String container)
    {
        return host.toAbsolutePath().toString().replace( "\\", "/" ) + ":" + container;
    }

}
