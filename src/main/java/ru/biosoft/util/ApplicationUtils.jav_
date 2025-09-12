package ru.biosoft.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Different utility functins.
 *
 * @pending need refactoring
 */
public class ApplicationUtils
{
    static Logger cat = Logger.getLogger( ApplicationUtils.class.getName() );

    ////////////////////////////////////////////////////////////////////////////
    // File utilities
    //

    public static void copyFile(String dst, String src) throws IOException
    {
        copyFile(
            new File(dst),
            new File(src));
    }

    public static BufferedReader utfReader(File file) throws IOException
    {
        return new BufferedReader( new InputStreamReader( new FileInputStream( file ), StandardCharsets.UTF_8 ) );
    }

    public static BufferedReader utfReader(String fileName) throws IOException
    {
        return utfReader( new File( fileName ) );
    }

    public static BufferedReader asciiReader(File file) throws IOException
    {
        return new BufferedReader( new InputStreamReader( new FileInputStream( file ), StandardCharsets.ISO_8859_1 ) );
    }

    public static BufferedReader asciiReader(String fileName) throws IOException
    {
        return asciiReader( new File( fileName ) );
    }

    public static BufferedWriter utfWriter(File file) throws IOException
    {
        return new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), StandardCharsets.UTF_8 ) );
    }

    public static BufferedWriter utfWriter(String fileName) throws IOException
    {
        return utfWriter( new File( fileName ) );
    }

    public static BufferedWriter utfAppender(File file) throws IOException
    {
        return new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file, true ), StandardCharsets.UTF_8 ) );
    }

    public static BufferedWriter utfAppender(String fileName) throws IOException
    {
        return utfAppender( new File( fileName ) );
    }

    public static BufferedWriter asciiWriter(File file) throws IOException
    {
        return new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), StandardCharsets.ISO_8859_1 ) );
    }

    public static BufferedWriter asciiWriter(String fileName) throws IOException
    {
        return asciiWriter( new File( fileName ) );
    }

    public static String readAsString(File src, int maxChars) throws IOException
    {
        char[] result = new char[maxChars];
        int offset = 0;
        try (FileInputStream in = new FileInputStream( src );
                InputStreamReader reader = new InputStreamReader( in, StandardCharsets.UTF_8 ))
        {
            while( true )
            {
                int read = reader.read(result, offset, maxChars-offset);
                if(read == -1)
                    return new String(result, 0, offset);
                offset+=read;
                if(offset >= maxChars)
                    return new String(result);
            }
        }
    }

    /**
     * Reads input stream into UTF-8 string
     * @param src
     * @return
     * @throws IOException
     */
    public static String readAsString(File file) throws IOException
    {
        return readAsString(new FileInputStream(file));
    }

    /**
     * Reads whole input file into memory, close original stream and return result as ByteArrayInputStream
     * @param file
     * @return
     * @throws IOException
     */
    public static ByteArrayInputStream readAsStream(File file) throws IOException
    {
        return readAsStream(new FileInputStream(file));
    }

    /**
     * Reads whole input stream into memory, close original stream and return result as ByteArrayInputStream
     * @param src
     * @return
     * @throws IOException
     */
    public static ByteArrayInputStream readAsStream(InputStream src) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ApplicationUtils.copyStream(baos, src);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Reads input stream into UTF-8 string
     * @param src
     * @return
     * @throws IOException
     */
    public static String readAsString(InputStream src) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ApplicationUtils.copyStream(baos, src);
        return baos.toString("UTF-8");
    }

    public static List<String> readAsList(File file) throws IOException
    {
        return readAsList(new FileInputStream(file));
    }

    public static List<String> readAsList(InputStream src) throws IOException
    {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(src, StandardCharsets.UTF_8)))
        {
            return reader.lines().collect(Collectors.toList());
        }
    }

    public static void writeString(OutputStream dst, String str) throws IOException
    {
        ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        ApplicationUtils.copyStream(dst, is);
    }

    public static void writeString(File dst, String str) throws IOException
    {
        writeString(new FileOutputStream(dst), str);
    }

    public static void copyStream(OutputStream dst, InputStream src) throws IOException
    {
        final int BUFFER_SIZE = 64 * 1024;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try
        {
            bis = src instanceof BufferedInputStream?(BufferedInputStream)src:new BufferedInputStream(src);
            bos = dst instanceof BufferedOutputStream?(BufferedOutputStream)dst:new BufferedOutputStream(dst);

            byte[] buffer = new byte [BUFFER_SIZE];
            int len;

            while( (len = bis.read(buffer)) != -1 )
            {
                bos.write(buffer, 0, len);
            }
        }
        finally
        {
            if (bis != null)
            {
                bis.close();
            }
            if (bos != null)
            {
                bos.flush();
                bos.close();
            }
        }
    }

    public static void copyStreamNoClose(OutputStream os, InputStream is) throws IOException
    {
        final int BUFFER_SIZE = 64 * 1024;
        BufferedInputStream bis = new BufferedInputStream( is );
        BufferedOutputStream bos = new BufferedOutputStream( os );

        byte[] buffer = new byte [BUFFER_SIZE];
        int len;

        while( (len = bis.read(buffer)) != -1 )
        {
            bos.write(buffer, 0, len);
        }
        bos.flush();
    }

    /**
     * Tries to create a hardlink first, copy if failed
     */
    public static void linkOrCopyFile(File dst, File src) throws IOException
    {
    	if(dst.getAbsolutePath().equals(src.getAbsolutePath()))
    	{
    		return;
    	}
        try
        {
            dst.delete();
            Files.createLink(dst.toPath(), src.toPath());
        }
        catch( Throwable e )
        {
            copyFile( dst, src );
        }
    }

    public static void copyFile(File dst, File src) throws IOException
    {
        try (FileInputStream source = new FileInputStream(src); FileOutputStream destination = new FileOutputStream(dst))
        {
            FileChannel sourceFileChannel = source.getChannel();
            FileChannel destinationFileChannel = destination.getChannel();

            long size = sourceFileChannel.size();
            long chunk = 10*1024*1024;
            for(long pos = 0; pos < size; pos+=chunk)
            {
                sourceFileChannel.transferTo(pos, Math.min(chunk, size-pos), destinationFileChannel);
            }
        }
    }

    /**
     * Copy folder recursively
     */
    public static void copyFolder(File dst, File src) throws Exception
    {
        if( src.isDirectory() )
        {
            dst.mkdir();
            for( File srcItem : src.listFiles() )
            {
                copyFolder(new File(dst, srcItem.getName()), srcItem);
            }
        }
        else
        {
            ApplicationUtils.copyFile(dst, src);
        }
    }

    public static void removeDir(File dir)
    {
        if (dir == null || !dir.isDirectory())
            return;
        File[] files = dir.listFiles();
        if (files != null)
        {
            for( File file : files )
            {
                if(file.isDirectory())
                {
                    removeDir(file);
                }
                if(!file.delete())
                {
                    file.deleteOnExit();
                }
            }
        }
        if(!dir.delete())
        {
            dir.deleteOnExit();
        }
    }

    public static List<File> getFiles(File dir, Set<String> excludedNames)
    {
        List<File> list = new ArrayList<>();
        if (dir.isDirectory())
        {
            File[] files = dir.listFiles();
            if (files != null)
            {

                for( File file : files )
                {
                    if (excludedNames == null || !excludedNames.contains(file.getName()))
                    {
                        list.add(file);
                        if(file.isDirectory())
                        {
                            list.addAll(getFiles(file, excludedNames));
                        }
                    }
                }
            }
        }
        return list;
    }

    public static List<File> getFiles(File dir)
    {
        return getFiles(dir, null);
    }

    public static void sortFiles(File[] files)
    {
        Arrays.sort( files, Comparator.comparing( File::getName ) );
    }

    public static String getRelativeFilePath(File parent, File file)
    {
        String relative = "";
        while( !file.equals(parent) )
        {
            relative = file.getName() + ( relative.isEmpty() ? "" : File.separator ) + relative;
            file = file.getParentFile();
            if( file == null )
                return null;
        }
        return relative;
    }

    ////////////////////////////////////////////////////////////////////////////
    // old utilities:
    // @pending - used only in ru.biosoft.bsa
    //

    public static String getLastToken(String str, String delimeters)
    {
        StringTokenizer strTok = new StringTokenizer(str, delimeters);
        String last = null;
        while (strTok.hasMoreTokens())
        {
            last = strTok.nextToken();
        }
        return last;
    }

    // TODO: support "/" in element names
    public static String getCommonParent(String name1, String name2)
    {
        String delim = "/\\";
        StringTokenizer strTok1 = new StringTokenizer(name1, delim);
        StringTokenizer strTok2 = new StringTokenizer(name2, delim);
        StringBuffer buffer = new StringBuffer();
        while (strTok1.hasMoreTokens() && strTok2.hasMoreTokens())
        {
            String token1 = strTok1.nextToken();
            String token2 = strTok2.nextToken();
            if (token1.equals(token2))
            {
                if (buffer.length() != 0)
                    buffer.append("/");
                buffer.append(token1);
            }
        }
        return buffer.toString();
    }

}
