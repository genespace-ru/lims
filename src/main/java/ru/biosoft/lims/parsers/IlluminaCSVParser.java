package ru.biosoft.lims.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IlluminaCSVParser
{

    private static final Pattern HEADER_PATTERN = Pattern.compile( "\\[(.+)\\]" );
    private static final char QUOTE_CHAR = '"';
    private static final char COMMA_CHAR = ',';

    private static final Set<String> keyValueSections = new HashSet<>( Arrays.asList( new String[] { "Header", "Settings", "Manifests" } ) );

    public static Map<String, Object> parseFile(File file) throws IOException
    {
        FileChannel ch = null;
        BufferedReader br;
        FileInputStream is = new FileInputStream( file );
        br = new BufferedReader( new InputStreamReader( is, Charset.forName( "UTF-8" ) ) );
        ch = is.getChannel();

        Map<String, Object> resMap = new HashMap<>();
        String line;
        String section = null;
        while ( true )
        {
            line = br.readLine();
            if( line == null )
                break;

            String wline = line.trim();
            if( wline.isEmpty() )
                continue;

            Matcher matcher = HEADER_PATTERN.matcher( wline );

            if( matcher.matches() )
            {
                section = matcher.group( 1 );
                continue;
            }

            String[] vals = splitWithQuotes( wline );

            /**
             * Header records are represented as a series of key-value pairs. As
             * such, each line requires exactly two fields. The first field in
             * each line is the "key," which names the piece of metadata being
             * recorded.
             * Each key in the Header section must be unique.
             * The second field in each line is the "value," which is the actual
             * piece of metadata being recorded. Values do not necessarily need
             * to be unique.
             */
            if( section == null )
                continue;
            else if( keyValueSections.contains( section ) )
            {
                HashMap<String, String> innermap = (HashMap<String, String>) resMap.computeIfAbsent( section, k -> new HashMap<String, String>() );
                innermap.put( vals[0], vals[1] );
            }

            else if( section.equals( "Reads" ) )
            {
                ((ArrayList<String>) resMap.computeIfAbsent( section, k -> new ArrayList<String>() )).add( vals[0] );
            }
            else if( section.equals( "Data" ) ) //last section
            {
                /**
                 * The line immediately following the Data section label is a
                 * headerline.
                 * The Data headerline lists the names of each of the table
                 * columns in this section, separated by commas. No specific
                 * ordering of the column names is required and they are not
                 * case-sensitive. Each column name may only appearin data
                 * headerline once.
                 */
                if( !resMap.containsKey( "Data" ) ) //first line of Data
                {
                    ArrayList<List> data = new ArrayList<List>();
                    data.add( Arrays.asList( vals ) );
                    resMap.put( "Data", data );
                }
                else //data rows
                {
                    ((ArrayList<List>) resMap.get( "Data" )).add( Arrays.asList( vals ) );
                }
            }
            else //Other used-defined sectoins or new sections by Illumina not described in format specifications
            {
                ((ArrayList<String>) resMap.computeIfAbsent( section, k -> new ArrayList<String>() )).add( wline );
            }

        }
        br.close();
        return resMap;
    }

    /**
     * Commas (ASCII code 44) are used as a delimiter to separate
     * adjacent fields on the same line. If a field contains a comma, then the
     * field needs to be wrapped in double quotes (ASCII code 34).
     * Example:
     * Library, type
     * is represented in the Sample Sheet as:
     * "Library, type"
     * If a field contains double quotes, then the entire field needs to be
     * wrapped in double quotes and the embedded double quotes need to
     * be escaped using a pair of adjacent double quotes.
     * Example:
     * This is in "quotes", as well as commas
     * is represented in the Sample Sheet as:
     * "This is in ""quotes"", as well as commas"
     * There is no global minimum or maximum number of comma delimiters
     * required to for each line. Use as many comma delimiters as
     * necessary to separate the number of fields required per line in a given
     * section. For instance, if a section of the sample sheet requires exactly
     * two fields per line, the minimum number of comma delimiters for that
     * line is one. However, the end of the line can be padded with as many
     * commas as desired, which are ignored.
     * 
     */
    private static String[] splitWithQuotes(String line)
    {
        ArrayList<String> values = new ArrayList<>();
        int state = 0;
        //0 - search for next word
        //1 - simple start, no quotes - go to next comma
        //2 - quoted start, go to next quote (without following quote)
        char[] chars = line.toCharArray();
        int len = chars.length;
        int i = 0;
        int start_pos = 0;
        while ( i < len )
        {
            if( state == 0 )
            {
                if( chars[i] == COMMA_CHAR )
                {
                    values.add( "" );
                    if( i == len - 1 )
                        values.add( "" );
                }
                else if( chars[i] == QUOTE_CHAR )
                {
                    state = 2;
                    start_pos = i + 1;
                }
                else
                {
                    if( i == len - 1 )
                        values.add( new String( chars, i, 1 ) );
                    else
                    {
                        state = 1;
                        start_pos = i;
                    }

                }
            }
            else if( state == 1 )
            {
                if( i == len - 1 ) //...
                {
                    values.add( new String( chars, start_pos, i - start_pos + 1 ) );
                    state = 0;
                }
                else if( chars[i] == COMMA_CHAR )
                {
                    values.add( new String( chars, start_pos, i - start_pos ) );
                    state = 0;
                }
            }
            else if( state == 2 )
            {
                if( chars[i] == QUOTE_CHAR )
                {
                    if( i == len - 1 ) //"..."
                    {
                        values.add( new String( chars, start_pos, i - start_pos ) );
                        state = 0;
                    }
                    else if( chars[i + 1] == COMMA_CHAR )
                    {
                        if( chars[i - 1] != QUOTE_CHAR )//"......",...
                        {
                            values.add( new String( chars, start_pos, i - start_pos - 1 ) );
                            state = 0;
                        }
                        else if( chars[i - 1] == QUOTE_CHAR && chars[i - 2] == QUOTE_CHAR ) // "....""...""",...
                        {
                            values.add( new String( chars, start_pos, i - start_pos - 1 ) );
                            state = 0;
                        }
                    }
                }
            }
            i++;

        }
        //return line.split( "," );
        return values.toArray( new String[0] );
    }

}
