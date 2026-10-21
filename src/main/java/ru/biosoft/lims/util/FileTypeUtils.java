package ru.biosoft.lims.util;

import com.developmentontheedge.be5.database.DbService;
import ru.biosoft.access.file.FileTypeRegistry;

import java.io.File;

/**
 * Utility for working with file types stored in the file_types dictionary.
 * Suffixes in file_types may be partial (e.g. ".fastq.gz", "_r1.fastq.gz"),
 * so matching uses LIKE against the end of the filename.
 */
public class FileTypeUtils {

    private FileTypeUtils() {
    }

    /**
     * Gets or creates a file type ID for the given file.
     * Matches suffixes using LIKE '%<suffix>' against the filename.
     *
     * @param db   the database service
     * @param file the file (used for name and text detection)
     * @return the file type ID
     */
    public static long getOrCreateTypeId(DbService db, File file) {
        String fileName = file.getName().toLowerCase();

        // Match suffixes using LIKE '%<suffix>' — longest suffix first for priority
        Long id = db.oneLong("SELECT id FROM file_types WHERE ? LIKE '%' || suffix ORDER BY LENGTH(suffix) DESC LIMIT 1", fileName);
        if (id != null) {
            return id;
        }

        // No match — determine if text or binary, create generic type if not exist
        boolean isText = FileTypeRegistry.isTextFile( file );
        return getOrCreateGenericTypeId( db, isText );
    }

    private static long getOrCreateGenericTypeId(DbService db, boolean isText)
    {
        String genericType;
        String description;
        if (isText) {
            genericType = ".text";
            description = "Generic text file";
        } else {
            genericType = ".binary";
            description = "Generic binary file";
        }

        Long id = db.oneLong( "SELECT id FROM file_types WHERE suffix = ?", genericType );
        if (id != null) {
            return id;
        }

        Long[] result = db.insert( "INSERT INTO file_types (suffix, description) VALUES (?, ?) RETURNING id", genericType, description );
        return result[0];
    }
}
