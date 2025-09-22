package ru.biosoft.lims;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

public class NextFlowTest
{

    private static String NEXT_FLOW_PATH = "fastqc.nf"; //relative path to nextflow script from workflows directory
    private static String CONFIG_PATH = "../nextflow.config"; //relative path to nextflow config from workflows directory

    private static String TOWER_ADDRESS = "http://172.24.112.1:8200/nf"; //ip needed here
    //    private static String TOWER_ADDRESS = "http://localhost:8200/nf"; //for some reason this does not work

    private static String WORK_DIR = "/mnt/c/users/damag/git/lims-test-hemotology/workflows/"; //path to lims-test-hematology workflows dir

    @Test
    public void runFastQCTest() throws Exception
    {
        //String[] command = new String[] {};

        boolean useWsl = System.getProperty( "os.name" ).startsWith( "Windows" );
        String[] command;
        if( useWsl )
            command = new String[] { "wsl", "--cd", WORK_DIR, "nextflow", NEXT_FLOW_PATH, "-c", CONFIG_PATH, "-with-tower", TOWER_ADDRESS };
        else
            command = new String[] { "nextflow", NEXT_FLOW_PATH, "-c", CONFIG_PATH, "-with-tower", TOWER_ADDRESS };
        runCommand(command);
    }

    private void runCommand(String[] command) throws Exception
    {

        Process process = Runtime.getRuntime().exec(command);

        new Thread(new Runnable()
        {
            public void run()
            {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;

                try
                {
                    while( ( line = input.readLine() ) != null )
                        System.out.println(line);
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                line = null;

                try
                {
                    while( ( line = err.readLine() ) != null )
                        System.out.println(line);
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }).start();

        process.waitFor();
    }

}