package ru.biosoft.nextflow;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;

import ru.biosoft.lims.SystemSettings;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import javax.inject.Inject;

public class NextflowService
{
	private static final Logger log = Logger.getLogger(NextflowService.class.getName() );

	@Inject
    private DbService db;

    @Inject 
    protected DatabaseModel database;

    public WorkflowInfo getWorkflowInfo(int workflowInfoId)
    {
    	String sql = """
    			SELECT wi.id, wi.title, fi.path 
    			FROM workflow_info wi
    			INNER JOIN file_info fi ON fi.entity = 'workflow_info' AND fi.entityID = ? """;
    	
    	QRec rec = db.recordWithParams(sql, workflowInfoId);
    	
    	WorkflowInfo wfInfo = new WorkflowInfo(rec.getInt("id"),
    			                               rec.getString("title"),
    			                               rec.getString("path"));  
    	return wfInfo;
    }

    protected synchronized int insertWorkflowRun(int projectId, int workflowInfoId)
    {
    	String sql = "INSERT INTO workflow_runs(project, workflow_info, status) VALUES(?, ?, 'preparation')";
    	Long id = (Long)db.insert(sql, projectId, workflowInfoId);
    	
    	return id.intValue();
    }

    protected void writeRunScript(File wfRunDir, int wfRunId, String wfRunIdStr, WorkflowInfo wfInfo) throws IOException
    {
    	File runScript = new File(wfRunDir, "run_" +  wfRunIdStr); 
    	runScript.createNewFile();
    	
    	try(PrintStream out = new PrintStream(runScript) )
    	{
    		out.print(
    				"#!/usr/bin/env bash\n" +
					"export TOWER_ACCESS_TOKEN=stub\n" +
					"export TOWER_WORKFLOW_ID=" + wfRunId + "\n\n" +
			    	
					SystemSettings.getNextflowDir(true) + "/nextflow run " +
					SystemSettings.getDataDir(true) + wfInfo.path() + 
					" -with-tower '" + SystemSettings.getNextflowTraceAPI() + "'\n" );
    	}
    }
    
    protected void writeConfig(File wfRunDir, String projectName) throws IOException
    {
    	File config = new File(wfRunDir, "nextflow.config"); 
    	config.createNewFile();
    	
    	try(PrintStream out = new PrintStream(config) )
    	{
    		out.print(
    				"docker.enabled = true\n" +
    				"tower.accessToken='stub'\n" +
    				"tower.workspaceId='stub'\n\n" +

    				"params.projectDir = '" + SystemSettings.getProjectsDir(true) + projectName + "/'");
    	}
    }
    
    /**
     * Starts workflow 
     */
    public int runWorkflow(int projectId, int workflowInfoId) throws IOException
    {
    	WorkflowInfo wfInfo = getWorkflowInfo(workflowInfoId);
    	int wfRunId = insertWorkflowRun(projectId, workflowInfoId);
    	String wfRunIdStr = String.format("%06d", wfRunId);

    	String projectName = db.getString("SELECT name FROM projects WHERE id=?", projectId); 
    	
    	String wfRunDirName = SystemSettings.getWorkflowRunsDir() + wfRunIdStr;
    	File wfRunDir = new File(wfRunDirName);
    	wfRunDir.mkdirs();

    	writeRunScript(wfRunDir, wfRunId, wfRunIdStr, wfInfo);    	
    	writeConfig(wfRunDir, projectName);    	
  	
    	return wfRunId;
    }
}
