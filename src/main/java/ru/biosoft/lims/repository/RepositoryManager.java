package ru.biosoft.lims.repository;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.developmentontheedge.be5.database.DbService;

import ru.biosoft.access.core.CollectionFactory;
import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataCollectionConfigConstants;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.core.FolderCollection;
import ru.biosoft.access.core.RepositoryException;
import ru.biosoft.access.core.SymbolicLinkDataCollection;
import ru.biosoft.access.core.TransformedDataCollection;
import ru.biosoft.access.file.FileBasedCollection;
import ru.biosoft.access.file.FileDataElement;
import ru.biosoft.access.file.GenericFileDataCollection;
import ru.biosoft.exception.ExceptionDescriptor;
import ru.biosoft.exception.ExceptionRegistry;

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

    public @Nonnull DataCollection<DataElement> createSubCollection(DataElementPath path) throws Exception
    {
        return createSubCollection( path, CreateStrategy.REMOVE_WRONG_TYPE, FolderCollection.class );
    }

    public @Nonnull DataCollection<DataElement> createSubCollection(DataElementPath path, boolean removeExisting) throws Exception
    {
        CreateStrategy strategy = removeExisting ? CreateStrategy.REMOVE_WRONG_TYPE : CreateStrategy.FAIL_IF_EXIST;
        return createSubCollection( path, strategy, FolderCollection.class );

    }

    public enum CreateStrategy
    {
        FAIL_IF_EXIST, REMOVE_WRONG_TYPE, FORCE_REMOVE
    }

    /**
     * Creates new GenericDataCollection
     * 
     * @param path path for new collection. Parent path must exist and must be
     * GenericDataCollection (possibly wrapped)
     * @param strategy strategy to use if element exists<br>
     * - FAIL_IF_EXIST: exception will be thrown if element already exists<br>
     * - REMOVE_WRONG_TYPE: existing element will be removed only if it has
     * wrong type<br>
     * - FORCE_REMOVE: existing element will be removed<br>
     * @param targetClass
     * @return
     * @throws Exception
     */
    public static @Nonnull DataCollection<DataElement> createSubCollection(DataElementPath path, @Nonnull CreateStrategy strategy, Class<? extends FolderCollection> targetClass)
            throws Exception
    {
        DataCollection<DataElement> parentCollection = path.getParentCollection();
        //TODO: Protected elements
        FolderCollection collection = doFetchPrimaryElement( parentCollection, Permission.WRITE ).cast( FolderCollection.class );
        String name = path.getName();
        DataElement de = doFetchPrimaryElement( path.optDataElement(), Permission.WRITE );
        if( de != null )
        {
            switch (strategy)
            {
            case FAIL_IF_EXIST:
                throw new DataElementExistsException( path );
            case REMOVE_WRONG_TYPE:
                if( targetClass.isInstance( de ) )
                    return (DataCollection<DataElement>) de;
                break;
            case FORCE_REMOVE:
                break;
            default:
                break;
            }
            collection.remove( name );
        }
        if( de != null && CreateStrategy.FAIL_IF_EXIST == strategy )
            throw new DataElementExistsException( path );
        collection.createSubCollection( name, targetClass );
        return path.getDataElement( ru.biosoft.access.core.DataCollection.class );
    }

    private static DataElement doFetchPrimaryElement(DataElement parent, int access)
    {
        if( !(parent instanceof ProtectedElement) )
            return parent;
        return ((ProtectedElement) parent).getUnprotectedElement( access );
    }

    public static File getChildFile(DataCollection<?> collection, String name)
    {
        collection = getTypeSpecificCollection( collection, FileDataElement.class );
        if( collection instanceof TransformedDataCollection )
        {
            collection = ((TransformedDataCollection<?, ?>) collection).getPrimaryCollection();
        }
        return ((FileBasedCollection<?>) collection).getChildFile( name );
    }

    public static DataCollection<?> getTypeSpecificCollection(DataCollection<?> parent, Class<? extends DataElement> clazz)
    {
        return getTypeSpecificCollection( parent, clazz, Permission.WRITE );
    }

    private static DataCollection getTypeSpecificCollection(DataCollection<?> parent, Class<? extends DataElement> clazz, int access)
    {
        parent = (DataCollection<?>) doFetchPrimaryElement( parent, access );
        if( parent instanceof SymbolicLinkDataCollection )
        {
            parent = ((SymbolicLinkDataCollection) parent).getPrimaryCollection();
            parent = (DataCollection<?>) doFetchPrimaryElement( parent, access );
        }
        //TODO: !!!!!!!!!!!!!
        /*
         * if( parent instanceof GenericDataCollection )
         * {
         * parent = ((GenericDataCollection) parent).getTypeSpecificCollection(
         * clazz );
         * }
         */
        return parent;
    }

    public static class DataElementExistsException extends RepositoryException
    {
        public static final ExceptionDescriptor ED_EXISTS = new ExceptionDescriptor( "Exists", LoggingLevel.Summary, "Element already exists: $path$" );

        public DataElementExistsException(DataElementPath path)
        {
            super( null, ED_EXISTS, path );
        }
    }

    public static class Permission
    {
        public static final int INFO = 0b00001;
        public static final int READ = 0b00010;
        public static final int WRITE = 0b00100;
        public static final int DELETE = 0b01000;
        public static final int ADMIN = 0b10000;
        public static final int ALL = 0b11111;
    }

}
