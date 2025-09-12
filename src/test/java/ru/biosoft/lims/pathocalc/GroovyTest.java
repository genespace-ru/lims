package ru.biosoft.lims.pathocalc;

import static ru.biosoft.lims.pathocalc.ACMGCriteria.*;
import static ru.biosoft.lims.pathocalc.ACMGCriteriaValue.State.*;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.jupiter.api.Test;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyTest {
	@Test
	public void test() throws CompilationFailedException, IOException {
		ClassLoader classLoader = GroovyTest.class.getClassLoader();
		Binding binding = new Binding();
		Map<ACMGCriteria, ACMGCriteriaValue> ACMG = createACMG();
		binding.setVariable("ACMG", ACMG);
		GroovyShell shell = new GroovyShell(classLoader, binding);
		Object result = shell.evaluate(new File("PathoCalc.groovy"));
		System.out.println("PathoCalc: "+result);
	}

	private Map<ACMGCriteria, ACMGCriteriaValue> createACMG() {
		Map<ACMGCriteria, ACMGCriteriaValue> ACMG = new EnumMap<ACMGCriteria, ACMGCriteriaValue>(ACMGCriteria.class);
		//initially set all criteria as UNMET
		for(ACMGCriteria c : ACMGCriteria.values())
			addCriteria(ACMG, c, false);
		//Add MET criteria
		addCriteria(ACMG, PVS1, true);
		addCriteria(ACMG, PS1, true);
		return ACMG;
	}
	private void addCriteria(Map<ACMGCriteria, ACMGCriteriaValue> ACMG, ACMGCriteria criteria, boolean met)
	{
		ACMG.put(criteria, new ACMGCriteriaValue(criteria, met?MET:UNMET));
	}
}
