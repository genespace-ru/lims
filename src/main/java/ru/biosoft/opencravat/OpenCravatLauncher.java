package ru.biosoft.opencravat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class OpenCravatLauncher {
	
	private static final Logger log = Logger.getLogger(OpenCravatLauncher.class.getName() );
	
	private String OPENCRAVAT_ON_HOST_DIR;
	private String modulesDir;
	private String workDir;
	
	private Map<String,String> annotators = new LinkedHashMap<>();
    private Map<String, String> reporters = new LinkedHashMap<>();
    private static OpenCravatLauncher launcherInstance = null;
	
    public OpenCravatLauncher()
    {
    }
	
    public static synchronized OpenCravatLauncher getInstance(String onHostDir) throws IOException, InterruptedException
    {
        if( launcherInstance == null )
        {
            launcherInstance = new OpenCravatLauncher();
            launcherInstance.init( onHostDir );
        }
        return launcherInstance;
	}
	
	public void init() throws IOException, InterruptedException
	{
		init(System.getProperty("OPENCRAVAT_ON_HOST_DIR"));
	}

	public void init(String onHostDir) throws IOException, InterruptedException
	{
		OPENCRAVAT_ON_HOST_DIR = onHostDir;
		if(OPENCRAVAT_ON_HOST_DIR == null || OPENCRAVAT_ON_HOST_DIR.trim().isEmpty())
			OPENCRAVAT_ON_HOST_DIR = new File("opencravat").getAbsolutePath();
		if(!new File(OPENCRAVAT_ON_HOST_DIR).exists())
			Files.createDirectories(Paths.get(OPENCRAVAT_ON_HOST_DIR));
		modulesDir=new File(OPENCRAVAT_ON_HOST_DIR, "modules").getAbsolutePath();
		if(!new File(modulesDir).exists())
			Files.createDirectory(Paths.get(modulesDir));
		workDir = new File(OPENCRAVAT_ON_HOST_DIR, "workdir").getAbsolutePath();
		if(!new File(workDir).exists())
			Files.createDirectory(Paths.get(workDir));

        //Install modules directly on server only once
        //installModules();
		
		initAnnotators();
        initReporters();
	}

	private void initAnnotators() throws IOException, InterruptedException {
        ProcessResult result = runInDockerSimple( "oc module ls -t annotator" );
		List<String> lines = result.stdoutLines;
		if(lines.isEmpty())
			return;
        //fixed-width column processing
		String header = lines.get(0);
		int nameIdx = findColumnStart(header, "Name");
		int titleIdx = findColumnStart(header, "Title");
		int typeIdx = findColumnStart(header, "Type");
		for(int i = 1; i < lines.size(); i++)
		{
			String line = lines.get(i);
			if(line.trim().isEmpty())
				continue;
			String name = sliceColumn(line, nameIdx, titleIdx).trim();
			String title = sliceColumn(line, titleIdx, typeIdx).trim();
			if(name.isEmpty())
			{
				log.info("oc module ls: skipping line without a name: \"" + line + "\"");
				continue;
			}
			if(title.isEmpty())
				title = name;
			annotators.put(name, title);
		}

	}

    private void initReporters() throws IOException, InterruptedException
    {
        ProcessResult result = runInDockerSimple( "oc module ls -t reporter" );
        List<String> lines = result.stdoutLines;
        if( lines.isEmpty() )
            return;
        //fixed-width column processing
        String header = lines.get( 0 );
        int nameIdx = findColumnStart( header, "Name" );
        int titleIdx = findColumnStart( header, "Title" );
        int typeIdx = findColumnStart( header, "Type" );
        for ( int i = 1; i < lines.size(); i++ )
        {
            String line = lines.get( i );
            if( line.trim().isEmpty() )
                continue;
            String name = sliceColumn( line, nameIdx, titleIdx ).trim();
            String title = sliceColumn( line, titleIdx, typeIdx ).trim();
            if( name.isEmpty() )
            {
                log.info( "oc module ls: skipping line without a name: \"" + line + "\"" );
                continue;
            }
            if( title.isEmpty() )
                title = name;
            String type = name.replace( "reporter", "" );
            reporters.put( type, title );
        }
    }

	/**
	 * Returns the 0-based start offset of the given column label in a fixed-width
	 * header line (the first column position at or after which the label begins),
	 * or -1 if not found.
	 */
	private static int findColumnStart(String header, String label)
	{
		int from = 0;
		while(true)
		{
			int idx = header.indexOf(label, from);
			if(idx < 0)
				return -1;
			// The label must be a whole token (preceded by start-of-line or whitespace)
			if(idx == 0 || Character.isWhitespace(header.charAt(idx - 1)))
				return idx;
			from = idx + 1;
		}
	}

	/**
	 * Extracts the text of a fixed-width column: from {@code start} up to (not
	 * including) {@code end}, or to end-of-line if {@code end} is -1.
	 */
	private static String sliceColumn(String line, int start, int end)
	{
		if(start < 0 || start >= line.length())
			return "";
		int stop = (end > start) ? Math.min(end, line.length()) : line.length();
		return line.substring(start, stop);
	}

	public Map<String, String> getAnnotators()
	{
		return annotators;
	}
	
    public Map<String, String> getReporters()
    {
        return reporters;
    }

    private void installModules() throws IOException, InterruptedException
	{
		runInDockerSimpleWithLogger("oc module install-base");
		runInDockerSimpleWithLogger("oc module install -y tsvreporter");
		runInDockerSimpleWithLogger("oc module install -y alphamissense clinvar clinvar_acmg go spliceai");
	}
	
	
    public List<File> runOpencravat(File inputVCF, List<String> annotators, String reportType, File output, ProcessProgress progress) throws IOException, InterruptedException
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
		
        cmd.add( reportType );

		if(!annotators.isEmpty())
		{
			cmd.add("-a");
			for(String a : annotators)
				cmd.add(a);
		}

        AtomicInteger exitCode2 = new AtomicInteger();
		ProcessProgress wrapper = new ProcessProgress() {
			public void stdout(String msg) {progress.stdout(msg);};
			public void stderr(String msg) {progress.stderr(msg);};
			public void finished(int exitCode) {
                exitCode2.set( exitCode );
				progress.finished(exitCode);
			}
		};
		
		runInDocker(cmd.toArray(new String[0]), curWorkDir.toFile().getAbsolutePath(), progress);

        if( exitCode2.get() == 0 )
		{
            // Collect every result file the reporter produced: out.<type> and
            // out.*.<type> (e.g. for the tsv reporter: out.variant.tsv,
            // out.gene.tsv, out.sample.tsv, out.mapping.tsv; for vcf/txt just
            // out.vcf / out.txt). They go into `output` (the workflow_run_<id>
            // results dir), not its parent.
            return copyResultFiles( curWorkDir, reportType, output );
		}
		throw new IOException( "OpenCRAVAT run failed with exit code " + exitCode2.get() );
	}

	/**
	 * Copies every OpenCRAVAT result file of the given report type
	 * ({@code out.<type>} and {@code out.*.<type>}) from the run work dir into
	 * {@code resultsDir} and returns the copied files. Returns an empty list if
	 * none were produced.
	 */
	private List<File> copyResultFiles( Path workDir, String reportType, File resultsDir ) throws IOException
	{
		if( !resultsDir.exists() )
			Files.createDirectories( resultsDir.toPath() );
		List<File> copied = new ArrayList<>();
		String suffix = "." + reportType;
		for ( Path entry : Files.list( workDir ).toArray( Path[]::new ) )
		{
			String name = entry.getFileName().toString();
			// Match out.<type> and out.*.<type> (e.g. out.vcf, out.variant.tsv,
			// out.gene.tsv), excluding non-file outputs like out.sqlite / out.log.
			if ( name.startsWith( "out." ) && name.endsWith( suffix ) )
			{
				Files.copy( entry, new File( resultsDir, name ).toPath(), StandardCopyOption.REPLACE_EXISTING );
				copied.add( new File( resultsDir, name ) );
			}
		}
		return copied;
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
		
        AtomicInteger exitCode2 = new AtomicInteger();
		ProcessProgress result = new ProcessProgress( ) {
			public void stdout(String msg) {
				log.info(msg);
			};
			public void stderr(String msg) {
				log.severe(msg);
			}
			public void finished(int exitCode) {
                exitCode2.set( exitCode );
			};
		};
		runInDocker(cmdString.split(" "), null, result);
        if( exitCode2.get() != 0 )
			throw new IOException("Exit code " + exitCode2 + " cmd: " + cmdString);
	}

	private void runInDocker(String[] args, String openCravatWorkDir, ProcessProgress progress) throws IOException, InterruptedException {
		
		List<String> cmd = new ArrayList<>();
		cmd.add("docker");
		cmd.add("run");
		cmd.add("-v");
		cmd.add( modulesDir+":/mnt/modules" );
		if(openCravatWorkDir != null)
        {
            cmd.add( "-v" );
            cmd.add( openCravatWorkDir + ":/tmp/job" );
        }
        cmd.add( "karchinlab/opencravat:3.0.0" );
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
