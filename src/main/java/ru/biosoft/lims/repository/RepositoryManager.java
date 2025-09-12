package ru.biosoft.lims.repository;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.developmentontheedge.be5.database.DbService;

import biouml.workbench.perspective.PerspectiveRegistry;
import biouml.workbench.perspective.PropertiesPerspective;
import biouml.workbench.perspective.RepositoryTabInfo;
import one.util.streamex.StreamEx;
import ru.biosoft.access.DataCollectionUtils;
import ru.biosoft.access.core.CollectionFactory;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.file.FileBasedCollection;
import ru.biosoft.access.file.GenericFileDataCollection;
import ru.biosoft.exception.ExceptionRegistry;
import ru.biosoft.util.ApplicationUtils;
import ru.biosoft.util.TempFiles;
import ru.biosoft.util.archive.ArchiveEntry;
import ru.biosoft.util.archive.ArchiveFactory;
import ru.biosoft.util.archive.ArchiveFile;

public class RepositoryManager
{
    private static final Map<String, DataCollection<?>> repositoryMap = new HashMap<>();
    //private static final List<DataCollection<?>> repositories = new ArrayList<>();
    private DataCollection<?> projects = null;
    private DataCollection<?> databases = null;

    @Inject
    protected DbService db;

    private static boolean initialized = false;

    private void init()
    {
        if( initialized )
            return;
        try
        {
            initRepositories();
            initPerspective();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        initialized = true;

    }

    public void initRepositories() throws Exception
    {
        String dir = db.getString( "SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='projects_dir'" );
        if( dir == null )
            throw new NullPointerException( "Project repository is not specified in systemsettings." + System.lineSeparator()
                    + "Please specify project repository in systemsettings, section_name=lims, setting_name=projects_repo. " );

        initRepository( dir );
        projects = repositoryMap.get( dir );

        String dbDir = db.getString( "SELECT setting_value FROM systemsettings WHERE section_name='lims' AND setting_name='databases_dir'" );
        if( dbDir != null )
        {
            initRepository( dbDir );
            databases = repositoryMap.get( dbDir );
        }
    }

    public void initRepository(String path) throws Exception
    {
        if( !repositoryMap.containsKey( path ) )
        {
            DataCollection<?> dc = null;
            try
            {
                dc = CollectionFactory.createRepository( path );
            }
            catch (Exception e)
            {
                File file = new File( path, DataCollectionConfigConstants.DEFAULT_CONFIG_FILE );
                if( !file.exists() )
                {
                    dc = GenericFileDataCollection.initGenericFileDataCollection( null, new File( path ) );
                }
            }
            if( dc != null )
            {
                repositoryMap.put( path, dc );
                CollectionFactory.registerRoot( dc );
            }
        }
    }

    public void initRepository(List<String> paths) throws Exception
    {
        for ( String path : paths )
        {
            try
            {
                initRepository( path );
            }
            catch (Exception e)
            {
                System.out.println( "Unable to init " + path + ": " + ExceptionRegistry.log( e ) );
            }
        }
    }

    /**
     * Close all root collections
     */
    public void closeRepositories() throws Exception
    {
        if( projects != null )
            projects.close();
        if( databases != null )
            databases.close();
    }

    //return projects path
    public String getRepositoryPath()
    {
        init();
        if( projects == null )
            return null;
        else
            return projects.getCompletePath().toString();
    }

    public static File getChildFile(FileBasedCollection<?> collection, String name)
    {
        return ((FileBasedCollection<?>) collection).getChildFile( name );
    }

    public static void importArchiveFile(File file, DataCollection parent) throws Exception
    {
        ArchiveFile archiveFile = ArchiveFactory.getArchiveFile( file );
        if( archiveFile == null )
            throw new Exception( "Specified file is not an archive" );
        //archiveFile = new ComplexArchiveFile( archiveFile );
        int i = 0;
        ArchiveEntry entry;
        int numImported = 0;
        List<String> importErrors = new ArrayList<>();
        DataElementPath rootPath = parent.getCompletePath();
        while ( (entry = archiveFile.getNextEntry()) != null )
        {
            i++;
            if( entry.isDirectory() )
                continue;
            String[] pathFields = entry.getName().split( "[\\\\\\/]+" );
            String entryName = pathFields[pathFields.length - 1];
            DataElementPath entryPath = StreamEx.of( pathFields ).without( "." ).without( ".." ).remove( String::isEmpty ).foldLeft( rootPath, DataElementPath::getChildPath );
            DataCollectionUtils.createFoldersForPath( entryPath );

            if( entryPath.exists() )
                continue;

            InputStream is = entry.getInputStream();
            File tmpFile = TempFiles.file( "zipImport" + entryName, is );
            try
            {
                File entryFile = getChildFile( (FileBasedCollection<?>) parent, entryName );
                ApplicationUtils.copyFile( entryFile, tmpFile, null );
                numImported++;
            }
            catch (Exception e)
            {
                importErrors.add( e.getMessage() );
            }
            finally
            {
                tmpFile.delete();
            }
        }
        if( numImported == 0 )
        {
            throw new Exception( "No importable or valid files were found in archive. Please check your input file." );
        }
        if( !importErrors.isEmpty() )
        {
            throw new Exception( "Error importing archived data: \n" + String.join( "\n", importErrors ) );
        }
    }

    private void initPerspective()
    {
        Map<String, Object> properties = new HashMap<>();
        properties.put( "title", "Default" );
        List<RepositoryTabInfo> tabs = new ArrayList<>();
        if( databases != null )
        {
            Map<String, Object> tabProps = new HashMap<>();
            tabProps.put( "title", databases.getName() );
            tabProps.put( "databases", true );
            tabProps.put( "path", databases.getCompletePath().toString() );
            RepositoryTabInfo repoTab = new RepositoryTabInfo( tabProps );
            tabs.add( repoTab );
        }
        if( projects != null )
        {
            Map<String, Object> tabProps = new HashMap<>();
            tabProps.put( "title", projects.getName() );
            tabProps.put( "databases", false );
            tabProps.put( "path", projects.getCompletePath().toString() );
            RepositoryTabInfo repoTab = new RepositoryTabInfo( tabProps );
            tabs.add( repoTab );
        }
        properties.put( "repository", tabs );
        List<Map<String, Object>> actionRules = new ArrayList<>();
        String[] deny = new String[] { "*" };
        String[] allow = new String[] { "semantic_zoom_in", "semantic_zoom_out", "semantic_default", "semantic_detailed", "semantic_overview", "shift_backward", "shift_forward",
                "page_backward", "page_forward", "combine_tracks", "new_gc_track" };
        for ( String id : deny )
        {
            Map<String, Object> rule = new HashMap<>();
            rule.put( "name", "deny" );
            rule.put( "id", id );
            actionRules.add( rule );
        }
        for ( String id : allow )
        {
            Map<String, Object> rule = new HashMap<>();
            rule.put( "name", "allow" );
            rule.put( "id", id );
            actionRules.add( rule );
        }
        properties.put( "actions", actionRules.toArray( new Object[] {} ) );
        PerspectiveRegistry.registerPerspective( "Default", PropertiesPerspective.class.getName(), properties );
    }
}
