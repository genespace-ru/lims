package ru.biosoft.opencravat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class OpenCravatLauncher {
	
	private static final Logger log = Logger.getLogger(OpenCravatLauncher.class.getName() );
	
	
	private String OPENCRAVAT_ON_HOST_DIR;
	private String modulesDir;
	private String workDir;
	
	private Map<String,String> annotators = new LinkedHashMap<>();
	
	
	public OpenCravatLauncher(){
	}
	
	public void init() throws IOException, InterruptedException
	{
		OPENCRAVAT_ON_HOST_DIR = System.getProperty("OPENCRAVAT_ON_HOST_DIR");
		if(OPENCRAVAT_ON_HOST_DIR == null)
			OPENCRAVAT_ON_HOST_DIR = new File("opencravat").getAbsolutePath();
		if(!new File(OPENCRAVAT_ON_HOST_DIR).exists())
			Files.createDirectories(Paths.get(OPENCRAVAT_ON_HOST_DIR));
		modulesDir=new File(OPENCRAVAT_ON_HOST_DIR, "modules").getAbsolutePath();
		if(!new File(modulesDir).exists())
			Files.createDirectory(Paths.get(modulesDir));
		workDir = new File(OPENCRAVAT_ON_HOST_DIR, "workdir").getAbsolutePath();
		if(!new File(workDir).exists())
			Files.createDirectory(Paths.get(workDir));
		
		installModules();
		
		initAnnotators();
	}

	private void initAnnotators() throws IOException, InterruptedException {
		ProcessResult result = runInDockerSimple("oc module ls -t annotator");
		for(int i = 1; i < result.stdoutLines.size(); i++)
		{
			String line = result.stdoutLines.get(i);
			String[] parts = line.split("  *");
			String name = parts[0];
			String title = parts[1];
			annotators.put(name, title);
		}
		
	}
	
	public Map<String, String> getAnnotators()
	{
		return annotators;
	}
	
	private void installModules()
	{
		runInDockerSimpleWithLogger("oc module install-base");
		runInDockerSimpleWithLogger("oc module install -y tsvreporter");
		runInDockerSimpleWithLogger("oc module install -y alphamissense clinvar clinvar_acmg go spliceai");
	}
	
	
	public void runOpencravat(File inputVCF, List<String> annotators, File outputTSV, ProcessProgress progress)
	{
		Path curWorkDir = Files.createTempDirectory(Paths.get(workDir), "dir");
		Files.copy(inputVCF.toPath(), curWorkDir.resolve("in.vcf"));
		
		List<String> cmd = new ArrayList<>();
		cmd.add("oc");
		cmd.add("run");
		cmd.add("in.vcf");
		cmd.add("-l");
		cmd.add("hg38");
		cmd.add("-n");
		cmd.add("out");
		cmd.add("-t");
		cmd.add("tsv");
		
		if(!annotators.isEmpty())
		{
			cmd.add("-a");
			for(String a : annotators)
				cmd.add(a);
		}

		int exitCode2;
		ProcessProgress wrapper = new ProcessProgress() {
			public void stdout(String msg) {progress.stdout(msg);};
			public void stderr(String msg) {progress.stderr(msg);};
			public void finished(int exitCode) {
				exitCode2 = exitCode;
				progress.finished(exitCode);
			}
		};
		
		runInDocker(cmd.toArray(new String[0]), curWorkDir.toFile().getAbsolutePath(), progress);
		
		if(exitCode2 == 0)
		{
			Files.copy(curWorkDir.resolve("out.variant.tsv"), outputTSV.toPath());
		}
	}

	private ProcessResult runInDockerSimple(String cmdString) throws IOException, InterruptedException {
		ProcessResult result = new ProcessResult();
		runInDocker(cmdString.split(" "), null, result);
		if(result.exitCode != 0)
		{
			for(String line : result.stderrLines)
				log.severe(line);
			throw new IOException("Exit code " + result.exitCode + " cmd: " + cmdString);
		}
		return result;
	}
	
	private void runInDockerSimpleWithLogger(String cmdString) throws IOException, InterruptedException {
		
		int exitCode2;
		ProcessProgress result = new ProcessProgress( ) {
			public void stdout(String msg) {
				log.info(msg);
			};
			public void stderr(String msg) {
				log.severe(msg);
			}
			public void finished(int exitCode) {
				exitCode2 = exitCode;
			};
		};
		runInDocker(cmdString.split(" "), null, result);
		if(exitCode2 != 0)
			throw new IOException("Exit code " + exitCode2 + " cmd: " + cmdString);
	}

	private void runInDocker(String[] args, String openCravatWorkDir, ProcessProgress progress) throws IOException, InterruptedException {
		
		List<String> cmd = new ArrayList<>();
		cmd.add("docker");
		cmd.add("run");
		cmd.add("-v");
		cmd.add( modulesDir+":/mnt/modules" );
		if(openCravatWorkDir != null)
			cmd.add( openCravatWorkDir+":/tmp/job");
		cmd.add( "karchinlab/opencravat:latest" );
		for(String arg : args)
			cmd.add(arg);
		
		runProcess(cmd, progress);
	}

	private void runProcess(List<String> cmd, ProcessProgress progress) throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(cmd);
		Process proc = pb.start();
		InputReaderThread.read(proc.inputReader(), progress::stdout);
		InputReaderThread.read(proc.errorReader(), progress::stderr);
		int exitCode = proc.waitFor();
		progress.finished(exitCode);
	}
	
	
}
