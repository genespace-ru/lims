package ru.biosoft.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.biosoft.access.core.DataElement;
import ru.biosoft.access.core.DataElementPath;
import ru.biosoft.access.file.GenericFileDataCollection;

/**
 * Shared helpers for walking file-based repository collections.
 */
public class RepositoryFileUtils
{
    private RepositoryFileUtils()
    {
    }

    /**
     * Recursively collects items from a file-based data collection.
     *
     * @param folder the collection to start from (may be null or nonexistent, in which case nothing is added)
     * @param result map to fill: key = path (DataElementPath.toString()), value = absolute file path
     * @param includeFolders if true, sub-folders are added as entries (key = folder path, value = folder absolute path);
     *                       if false, only files are added. In both cases the walk descends into sub-folders
     *                       to reach nested files.
     */
    public static void collectItemsRecursive(GenericFileDataCollection folder, Map<String, String> result, boolean includeFolders)
    {
        if( folder == null || !folder.getCompletePath().exists() )
            return;
        for( String name : folder.getNameList() )
        {
            DataElementPath p = folder.getCompletePath().getChildPath( name );
            File f = folder.getFile( name );
            if( f != null && f.exists() )
            {
                DataElement child = p.getDataElement();
                if( child instanceof GenericFileDataCollection && p.exists() )
                {
                    GenericFileDataCollection subFolder = (GenericFileDataCollection) child;
                    if( includeFolders )
                    {
                        result.put( p.toString(), f.getAbsolutePath() );
                    }
                    collectItemsRecursive( subFolder, result, includeFolders );
                }
                else
                    result.put( p.toString(), f.getAbsolutePath() );
            }
        }
    }

    /**
     * Recursively copy src into dst, creating directories and overwriting
     * existing files (unlike Files.copy, which fails if dst already exists).
     */
    public static void copyDirOverwrite(Path src, Path dst) throws IOException
    {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) throws IOException
            {
                Files.createDirectories(dst.resolve(src.relativize(d)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException
            {
                Files.copy(f, dst.resolve(src.relativize(f)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Recursively collects relative paths of .wdl / .nf files under base,
     * skipping .git directories. Returned paths are relative to dir.
     */
    public static List<String> collectWorkflowFiles(Path base, Path dir) throws IOException
    {
        List<String> result = new ArrayList<>();
        Files.walkFileTree(base, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) throws IOException
            {
                if( ".git".equals(d.getFileName() == null ? null : d.getFileName().toString()) )
                    return FileVisitResult.SKIP_SUBTREE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                String fileName = file.getFileName().toString();
                if( fileName.endsWith(".wdl") || fileName.endsWith(".nf") )
                {
                    result.add(dir.relativize(file).toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    /**
     * Strip archive extensions from a file name (e.g. brca.tar.gz -> brca,
     * my.zip -> my). Longest suffixes are checked first so .tar.gz wins over .gz.
     */
    public static String stripArchiveExtensions(String name)
    {
        String[] exts = {".tar.gz", ".tar.bz2", ".tar.xz", ".tgz", ".tbz2", ".txz",
                         ".zip", ".tar", ".gz", ".bz2", ".xz", ".7z"};
        String lower = name.toLowerCase();
        for (String ext : exts)
        {
            if (lower.endsWith(ext) && name.length() > ext.length())
            {
                return name.substring(0, name.length() - ext.length());
            }
        }
        return name;
    }
}
