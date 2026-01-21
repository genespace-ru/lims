package ru.biosoft.nextflow;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import ru.biosoft.lims.DbTest;

/**
 * Tests run workflow.
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NextflowServiceTest extends DbTest
{
	NextflowService nfService;
	int workflowRunID;
	
    @Disabled
	@Test
    @Order(1) 
    public void startWorkflow() throws Exception
    {
    	nfService = getInjector().getInstance(NextflowService.class);

    	nfService.runWorkflow(1, 1);
    }

}