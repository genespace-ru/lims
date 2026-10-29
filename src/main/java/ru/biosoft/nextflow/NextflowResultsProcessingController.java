package ru.biosoft.nextflow;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.io.FilenameUtils;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.file.FileTypeRegistry;
import ru.biosoft.access.file.GenericFileDataCollection;
import ru.biosoft.lims.repository.RepositoryManager;
import ru.biosoft.util.ApplicationUtils;
import ru.biosoft.util.RepositoryFileUtils;

public class NextflowResultsProcessingController extends NextflowController
{

    private static final Logger log = Logger.getLogger(NextflowResultsProcessingController.class.getName() );
    
    @Inject 
    protected DbService db;
    
    @Inject
    private RepositoryManager repo;

    @Override
    protected String process(Request req, Response res, JsonObject body) throws Exception
    {
        String uri = req.getRequestUri();
        String[] tokens = uri.split("/"); 

        String method = req.getRawRequest().getMethod();
        
        if( "POST".equals( method ) && "parse".equals( tokens[2] ) )
        {
            if( "multiqc".equals( tokens[3] ) )
                return parseMultiQCResults( req, body );
            if( "results".equals( tokens[3] ) )
                return processWorkflowResults( req, body );
        }

        throw unknownRequest(req);
    }

    private String parseMultiQCResults(Request req, JsonObject body)
    {
        Integer workflowId = body.getInt( "workflowId" );
        Integer projectId = body.getInt( "prjId" );
        String repoPath = repo.getRepositoryPath();

        String projectName = db.getString( "SELECT name FROM projects WHERE ID=?", projectId );
        DataElementPath projectPath = DataElementPath.create( repoPath ).getChildPath( projectName );

        String resFile = body.getString( "results" );
        if( resFile != null )
        {
            File qcResults = new File( resFile );
            try
            {
                String res = ApplicationUtils.readAsString( qcResults );
                String[] resLines = res.split( "\n" );
                if( resLines.length > 0 )
                {

                    String[] columns = resLines[0].split( "\\s+" );
                    String sql = "INSERT INTO qc_runs(project, sample, file_info, workflow, qc_info) VALUES(?, ?, ?, ?, ?::jsonb)";
                    for ( int i = 1; i < resLines.length; i++ )
                    {
                        String[] values = resLines[i].split( "\\s+" );
                        JsonObjectBuilder job = Json.createObjectBuilder();
                        String fileName = null;
                        String sample = null;
                        int maxColNum = Math.min( columns.length, values.length );
                        for ( int j = 0; j < maxColNum; j++ )
                        {
                            if( columns[j].equalsIgnoreCase( "Filename" ) )
                                fileName = values[j];
                            else if( columns[j].equalsIgnoreCase( "sample" ) )
                                sample = values[j];
                            else
                                job.add( columns[j], values[j] );
                        }
                        JsonObject params = job.build();
                        Long sampleId = null;
                        if( sample != null )
                        {
                            sampleId = db.oneLong( "SELECT ID FROM samples WHERE title=? AND project=? ", sample, projectId );
                            if( sampleId == null )
                            {
                                //try to remove reads suffix
                                sample = sample.substring( 0, sample.lastIndexOf( "_" ) );
                                sampleId = db.oneLong( "SELECT ID FROM samples WHERE title=? AND project=? ", sample, projectId );
                            }
                        }
                        Long fileInfo = fileName != null ? db.oneLong( "SELECT ID FROM file_info WHERE filename=? AND project=? ", fileName, projectId ) : null;
                        db.updateRaw( sql, projectId, sampleId, fileInfo, workflowId, params.toString() );
                    }
                }

            }
            catch (IOException e)
            {
                log.warning( "Failed to read MultiQC results file: " + e.getMessage() );
            }
        }
        String reportFileStr = body.getString( "report" );
        if( reportFileStr != null )
        {
            File reportFile = new File( reportFileStr );
            File resultsFolder = ((GenericFileDataCollection) projectPath.getDataCollection()).getFile( "results" );
            Path pathRelative = resultsFolder.toPath().relativize( reportFile.toPath() );

            DataElementPath results = projectPath.getChildPath( "results" );
            String[] parts = pathRelative.toString().split( Pattern.quote( File.separator ) );
            DataElementPath reportPath = results.getChildPath( parts );

            Long fileTypeId = db.oneLong( "SELECT ID FROM file_types WHERE suffix=?", reportFile.getName() );
            if( reportPath.exists() )
            {
                db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)", reportFile.getName(), fileTypeId,
                        reportPath.toString(), reportFile.length(), projectId, "workflow_runs", workflowId );
            }

        }

        return "{ \"result\":\"ok\"}";

    }

    public String processWorkflowResults(Request req, JsonObject body)
    {
        Integer workflowRunId = body.getInt( "workflowId" );
        QRec wReq = db.recordWithParams( "SELECT pr.name, comment, pr.id as projectId FROM workflow_runs wr JOIN projects pr ON pr.id=wr.project WHERE wr.id=?", workflowRunId );
        if( wReq != null )
        {
            //Parse outputs.json and register files, if no outputs, parse folders and register files
            String projectName = wReq.getString( "name" );
            String runFolderPath = wReq.getString( "comment" );
            Long projectId = wReq.getLong( "projectId" );
            if( projectName != null && runFolderPath != null )
            {
                String repoPath = repo.getRepositoryPath();
                DataElementPath resultsPath = DataElementPath.create( repoPath ).getChildPath( projectName ).getChildPath( "results" ).getChildPath( runFolderPath );
                GenericFileDataCollection folder = resultsPath.getDataElement( GenericFileDataCollection.class );

                // Cache resolved file_type ids for the duration of this call so we don't
                // hit the DB once per output file for the same extension.
                Map<String, Long> fileTypeCache = new HashMap<>();

                File outputsPath = folder.getFile( "outputs.json" );
                //Here we need outputs.json containing absolute paths in projects/ProjectName/results/workflow_run folder
                //This folder is set as base publishDir in nextflow generation process 
                if( outputsPath.exists() )
                {
                    // The run folder this outputs.json belongs to is simply the parent of the file.
                    Path resultsPath2 = outputsPath.getParentFile().toPath();
                    try( JsonReader jsonReader = Json.createReader( new FileReader( outputsPath ) ) )
                    {
                        JsonObject outputs = jsonReader.readObject();
                        for ( Entry<String, JsonValue> entry : outputs.entrySet() )
                        {
                            if( !( entry.getValue() instanceof JsonObject ) )
                                continue;
                            JsonObject fileInfo = (JsonObject) entry.getValue();
                            String type = fileInfo.getString( "type", null );
                            //TODO: put non-file results somewhere
                            if( !"File".equals( type ) && !"Directory".equals( type ) )
                                continue;
                            // A WDL Directory output is published as a whole folder (e.g. a
                            // report with its css/images assets). outputs.json only stores a
                            // recursive listing for it (no root path), so resolve the published
                            // folder from the entry key (<workflow>.out.<taskName>) and register
                            // every file under it recursively.
                            if( "Directory".equals( type ) )
                            {
                                registerPublishedDirectory( entry.getKey(), folder, resultsPath2, projectId, workflowRunId, fileTypeCache );
                                continue;
                            }
                            String value = fileInfo.getString( "value", null );
                            if( value == null )
                                continue;
                            File resultFile = new File( value );
                            if( !resultFile.exists() )
                            {
                                log.warning( "Output file not found: " + value );
                                continue;
                            }
                            Path relPath = resultsPath2.relativize( resultFile.toPath() );
                            DataElementPath resPath = getFromRelativePath( relPath );
                            Long fileTypeId = getFileType( resultFile, FilenameUtils.getExtension( resultFile.getName() ), fileTypeCache );
                            db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)",
                                    resultFile.getName(), fileTypeId, resPath.toString(), resultFile.length(), projectId, "workflow_runs", workflowRunId );
                        }
                    }
                    catch ( IOException e )
                    {
                        log.warning( "Failed to parse outputs.json: " + e.getMessage() );
                    }
                }
                else
                {
                    // No outputs.json: register every file found on disk, plus each
                    // directory as a 'folder' row so the folder structure is browsable.
                    Map<String, String> allFiles = new LinkedHashMap<>();
                    RepositoryFileUtils.collectItemsRecursive( folder, allFiles, false );
                    for ( Entry<String, String> file : allFiles.entrySet() )
                    {
                        File resultFile = new File( file.getValue() );
                        Long fileTypeId = getFileType( resultFile, FilenameUtils.getExtension( file.getValue() ), fileTypeCache );
                        db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)", resultFile.getName(), fileTypeId,
                                file.getKey(), resultFile.length(), projectId, "workflow_runs", workflowRunId );
                    }
                    Long folderTypeId = getOrCreateFolderTypeId();
                    Map<String, String> allFolders = new LinkedHashMap<>();
                    RepositoryFileUtils.collectItemsRecursive( folder, allFolders, true );
                    for ( Entry<String, String> dirEntry : allFolders.entrySet() )
                    {
                        File dirFile = new File( dirEntry.getValue() );
                        if( !dirFile.isDirectory() )
                            continue;
                        String relPath = dirEntry.getKey();
                        // Skip the run folder itself — it is the results root, not a result.
                        if( relPath.isEmpty() )
                            continue;
                        db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)",
                                dirFile.getName(), folderTypeId, relPath, 0L, projectId, "workflow_runs", workflowRunId );
                    }
                }

                // After file registration, check for a MultiQC FastQC summary and
                // parse it into qc_runs so per-sample QC metrics are available in
                // the Quality control view.
                parseMultiQCIfPresent( folder, projectId, workflowRunId );
            }
        }
        return "{ \"result\":\"ok\"}";
    }

    /**
     * If the run folder contains a MultiQC FastQC summary (multiqc_data/multiqc_fastqc.txt),
     * parse it and insert per-sample QC metrics into the qc_runs table.
     *
     * This replicates the logic of {@link #parseMultiQCResults} but reads the summary
     * from the run folder on disk rather than from a file path in a POST body, so it
     * works for any workflow that publishes a MultiQC FastQC report — not just the
     * legacy QualityControl button.
     */
    private void parseMultiQCIfPresent( GenericFileDataCollection runFolder, Long projectId, Integer workflowRunId )
    {
        File summaryFile = runFolder.getFile( "multiqc_data/multiqc_fastqc.txt" );
        if( !summaryFile.exists() )
            return;

        try
        {
            String content = ApplicationUtils.readAsString( summaryFile );
            String[] lines = content.split( "\n" );
            if( lines.length < 2 )
                return;

            String[] columns = lines[0].split( "\\s+" );
            String sql = "INSERT INTO qc_runs(project, sample, file_info, workflow, qc_info) VALUES(?, ?, ?, ?, ?::jsonb)";

            for ( int i = 1; i < lines.length; i++ )
            {
                String line = lines[i].trim();
                if( line.isEmpty() )
                    continue;

                String[] values = line.split( "\\s+" );
                JsonObjectBuilder job = Json.createObjectBuilder();
                String fileName = null;
                String sample = null;
                int maxColNum = Math.min( columns.length, values.length );
                for ( int j = 0; j < maxColNum; j++ )
                {
                    if( columns[j].equalsIgnoreCase( "Filename" ) )
                        fileName = values[j];
                    else if( columns[j].equalsIgnoreCase( "sample" ) )
                        sample = values[j];
                    else
                        job.add( columns[j], values[j] );
                }
                JsonObject params = job.build();

                Long sampleId = null;
                if( sample != null )
                {
                    sampleId = db.oneLong( "SELECT ID FROM samples WHERE title=? AND project=? ", sample, projectId );
                    if( sampleId == null && sample.lastIndexOf( "_" ) > 0 )
                    {
                        // try to strip the reads suffix (e.g. Sample_1 -> Sample)
                        String stripped = sample.substring( 0, sample.lastIndexOf( "_" ) );
                        sampleId = db.oneLong( "SELECT ID FROM samples WHERE title=? AND project=? ", stripped, projectId );
                    }
                }

                Long fileInfoId = fileName != null
                        ? db.oneLong( "SELECT ID FROM file_info WHERE filename=? AND project=? ", fileName, projectId )
                        : null;

                db.updateRaw( sql, projectId, sampleId, fileInfoId, workflowRunId, params.toString() );
            }
        }
        catch ( Exception e )
        {
            log.warning( "Failed to parse MultiQC FastQC summary into qc_runs: " + e.getMessage() );
        }
    }

    /**
     *
     * outputs.json keys directory entries as "&lt;workflow&gt;.out.&lt;taskName&gt;". The
     * generated Nextflow script publishes each task to "&lt;publishDir&gt;/&lt;taskName&gt;",
     * so the published folder is the run folder with the task name appended. We walk it
     * recursively and insert a file_info row per file, mirroring the no-outputs.json
     * fallback branch above.
     */
    private void registerPublishedDirectory( String outputKey, GenericFileDataCollection runFolder,
            Path resultsPath2, Long projectId, Integer workflowRunId, Map<String, Long> fileTypeCache )
    {
        int outIdx = outputKey.lastIndexOf( ".out." );
        String taskName = ( outIdx >= 0 && outIdx + 5 < outputKey.length() )
                ? outputKey.substring( outIdx + 5 )
                : outputKey;
        DataElementPath dirPath = runFolder.getCompletePath().getChildPath( taskName );
        File dirFile = runFolder.getFile( taskName );
        if( dirFile == null || !dirFile.isDirectory() )
        {
            log.warning( "Published directory for output '" + outputKey + "' not found: " + ( dirFile == null ? "<null>" : dirFile.getAbsolutePath() ) );
            return;
        }
        GenericFileDataCollection dirFolder = dirPath.getDataElement( GenericFileDataCollection.class );
        Map<String, String> allFiles = new LinkedHashMap<>();
        RepositoryFileUtils.collectItemsRecursive( dirFolder, allFiles, false );
        for ( Entry<String, String> file : allFiles.entrySet() )
        {
            File resultFile = new File( file.getValue() );
            if( !resultFile.exists() )
                continue;
            Path relPath = resultsPath2.relativize( resultFile.toPath() );
            DataElementPath resPath = getFromRelativePath( relPath );
            Long fileTypeId = getFileType( resultFile, FilenameUtils.getExtension( resultFile.getName() ), fileTypeCache );
            db.insert( "INSERT INTO file_info (filename, filetype, path, size, project, entity, entityID) VALUES(?,?,?,?,?,?,?)",
                    resultFile.getName(), fileTypeId, resPath.toString(), resultFile.length(), projectId, "workflow_runs", workflowRunId );
        }
    }

    /**
     * Gets or creates the 'folder' file type in the file_types dictionary.
     * Used to register directories in file_info.
     */
    private Long getOrCreateFolderTypeId()
    {
        Long id = db.oneLong( "SELECT id FROM file_types WHERE suffix = ?", "folder" );
        if( id != null )
            return id;
        Long[] result = db.insert( "INSERT INTO file_types (suffix, description) VALUES (?, ?) RETURNING id", "folder", "Folder" );
        return result[0];
    }

    private long getFileType(File file, String fileExt, Map<String, Long> cache)
    {
        String ext = fileExt == null ? "" : fileExt;
        // Fast path: the extension has a direct file_types match — result is purely
        // extension-determined, so cache under the bare extension.
        Long byExt = cache.get( ext );
        if( byExt != null )
            return byExt;

        Long typeID = db.oneLong( "SELECT id FROM file_types WHERE suffix = ?", ext );
        if( typeID == null )
        {
            // No direct match: fall back to a generic type based on whether this file
            // is text or binary. That depends on file content, so include it in the key.
            String genericType = FileTypeRegistry.isTextFile( file ) ? ".text" : ".binary";
            String key = ext + "|" + genericType;
            Long byGeneric = cache.get( key );
            if( byGeneric != null )
                return byGeneric;
            typeID = db.oneLong( "SELECT id FROM file_types WHERE suffix = ?", genericType );
            if( typeID == null )
            {
                String descr = genericType.equals( ".text" ) ? "Generic text file" : "Generic binary file";
                typeID = db.insert( "INSERT INTO file_types (suffix, description) VALUES (?,?)", genericType, descr );
            }
            cache.put( key, typeID );
        }
        else
        {
            cache.put( ext, typeID );
        }
        return typeID;
    }

    private DataElementPath getFromRelativePath(Path path)
    {
        if( path == null )
            return DataElementPath.EMPTY_PATH;
        DataElementPath dePath = DataElementPath.EMPTY_PATH;
        for ( Path component : path )
        {
            dePath = dePath.getChildPath( component.toString() );
        }
        return dePath;

    }

}
