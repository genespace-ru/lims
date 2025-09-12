package ru.biosoft.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ru.biosoft.access.core.DataCollection;
import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementCreateException;
import ru.biosoft.access.core.DataElementNotFoundException;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.core.FolderCollection;
import ru.biosoft.access.core.RepositoryException;
import ru.biosoft.exception.ExceptionDescriptor;
import ru.biosoft.exception.MissingParameterException;

/**
 * @pending need refactoring
 */
public class DataCollectionUtils
{
/**
     * Tries to create GenericDataCollection parent folders for given path
     * @param path
     */
    public static void createFoldersForPath(DataElementPath path)
    {
        if( path.optParentCollection() == null )
        {
            // Trying to create missing directories
            List<ru.biosoft.access.core.DataElementPath> parentPaths = new ArrayList<>();
            DataElementPath parentPath = path.getParentPath();
            while( !parentPath.exists() && !parentPath.isEmpty() )
            {
                parentPaths.add(0, parentPath);
                parentPath = parentPath.getParentPath();
            }
            if( parentPath.isEmpty() )
            {
                if( parentPaths.isEmpty() )
                    throw new DataElementCreateException( new MissingParameterException( "Path" ), path.getParentPath(),
                            FolderCollection.class);
                throw new DataElementCreateException(new DataElementNotFoundException(parentPaths.get(0)), path.getParentPath(),
                        FolderCollection.class);
            }
            for( DataElementPath curPath : parentPaths )
            {
                try
                {
                    createSubCollection(curPath);
                }
                catch( Exception e )
                {
                    if( !curPath.exists() )
                        throw new DataElementCreateException(e, curPath, FolderCollection.class);
                }
            }
        }
    }

    public static @Nonnull DataCollection<DataElement> createSubCollection(DataElementPath path) throws Exception
    {
        return createSubCollection( path, CreateStrategy.REMOVE_WRONG_TYPE, FolderCollection.class );
    }

    public static @Nonnull DataCollection<DataElement> createSubCollection(DataElementPath path, boolean removeExisting) throws Exception
    {
        CreateStrategy strategy = removeExisting ? CreateStrategy.REMOVE_WRONG_TYPE : CreateStrategy.FAIL_IF_EXIST;
        return createSubCollection( path, strategy, FolderCollection.class );

    }

    public static enum CreateStrategy
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
        FolderCollection collection = (FolderCollection) parentCollection;
        String name = path.getName();
        DataElement de = path.optDataElement();
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

    public static class DataElementExistsException extends RepositoryException
    {
        public static final ExceptionDescriptor ED_EXISTS = new ExceptionDescriptor( "Exists", LoggingLevel.Summary, "Element already exists: $path$" );

        public DataElementExistsException(DataElementPath path)
        {
            super( null, ED_EXISTS, path );
        }
    }
}