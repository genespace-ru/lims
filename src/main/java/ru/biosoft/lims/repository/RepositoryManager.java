package ru.biosoft.lims.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;


import com.developmentontheedge.be5.database.DbService;

import one.util.streamex.StreamEx;
import ru.biosoft.access.core.CollectionFactory;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.file.FileBasedCollection;
import ru.biosoft.access.file.GenericFileDataCollection;
import ru.biosoft.exception.ExceptionRegistry;
import ru.biosoft.util.ApplicationUtils;
import ru.biosoft.util.DataCollectionUtils;
import ru.biosoft.util.TempFiles;
import ru.biosoft.util.archive.ArchiveEntry;
import ru.biosoft.util.archive.ArchiveFactory;
import ru.biosoft.util.archive.ArchiveFile;
import ru.biosoft.util.archive.ComplexArchiveFile;

public class RepositoryManager
{
    private static final Map<String, DataCollection<?>> repositoryMap = new HashMap<>();

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
        for ( DataCollection<?> dc : repositoryMap.values() )
            dc.close();
    }

    public Set<String> getRepositoryPaths()
    {
        return repositoryMap.keySet();
    }

    public String getRepositoryPath()
    {
        init();
        if( repositoryMap.isEmpty() )
            return null;
        else
            return repositoryMap.values().iterator().next().getCompletePath().toString();
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
                ApplicationUtils.copyFile( entryFile, tmpFile );
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
}
