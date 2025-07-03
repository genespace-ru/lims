package ru.biosoft.lims.parsers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YamlParser
{
    @SuppressWarnings ( "unchecked" )
    public static Map<String, Object> parseFile(File file) throws IOException
    {
        byte[] bytes = Files.readAllBytes( file.toPath() );
        String text = new String( bytes );
        Yaml parser = new Yaml();

        Object root = parser.load( text );
        if( ! ( root instanceof Map ) )
            throw new IllegalArgumentException("Yaml should be a map of key-values, but get " + root);

        Map<?, ?> rootMap = (Map<?, ?>)root;
        return (Map<String, Object>)rootMap;
    }
}
