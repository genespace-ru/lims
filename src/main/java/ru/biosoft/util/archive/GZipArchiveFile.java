package ru.biosoft.util.archive;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.input.CountingInputStream;

/**
 * @author lan
 *
 */
public class GZipArchiveFile implements ArchiveFile
{
    private GzipCompressorInputStream inputStream;
    private ArchiveEntry entry;
    private boolean read = false;
    private CountingInputStream cis;
    
    public GZipArchiveFile(String fileName, BufferedInputStream bis)
    {
        try
        {
            if( !fileName.toLowerCase().endsWith( ".gz" ) )
                return;
            String name = fileName.substring( 0, fileName.length() - 3 );
            bis.mark(8192);
            byte[] header = new byte[10];
            bis.read(header);
            if( header[0] != (byte)0x1F || header[1] != (byte)0x8B )
                throw new UnsupportedOperationException();
            bis.reset();
            cis = new CountingInputStream(bis);
            inputStream = new GzipCompressorInputStream(cis, true);
            entry = new ArchiveEntry(name, false, -1, inputStream);
        }
        catch(Exception e)
        {
            try
            {
                bis.reset();
            }
            catch( IOException e1 )
            {
            }
        }
    }

    @Override
    public boolean isValid()
    {
        return entry != null;
    }

    @Override
    public void close()
    {
        try
        {
            inputStream.close();
        }
        catch( IOException e )
        {
        }
    }

    @Override
    public ArchiveEntry getNextEntry()
    {
        if(read) return null;
        read = true;
        return entry;
    }

    @Override
    public long offset()
    {
        return cis.getByteCount();
    }
}
