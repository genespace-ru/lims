package operations

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.server.model.Base64File
import com.developmentontheedge.be5.server.operations.support.GOperationSupport

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.BufferedReader

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
	
import static groovy.json.JsonOutput.toJson

class UploadFile extends GOperationSupport
{
    Map<String, Object> presets

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        presets = presetValues

        params.type = [ value: null, 
             DISPLAY_NAME: "Type", TYPE: String,
             TAG_LIST_ATTR: [ "VEP", "unknown" ] as String[] ]

        params.file = [ TYPE: Base64File, DISPLAY_NAME: "File name" ]

        return DpsUtils.setValues(params, presetValues)
    }

    def processVCF( file, tableName ) 
    {
        def header = []
        
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file.data))).withReader { reader ->
            reader.eachLine { line ->
                if( line.startsWith("##") ) 
                {
                    return
                }

                if( line.startsWith("#") ) 
                {
                    header = line.substring(1).split("\t")
                    createTable(tableName, header)
                } 
                else 
                {
                    def row = line.split("\t")
                    def extraIndex = header.findIndexOf { it == 'Extra' }
                    if( extraIndex != -1 ) 
                    {
                        def input = row[ extraIndex ]
                        def map = input.split(';').collectEntries { entry ->
                            def (key, value) = entry.split('=')
                            [(key): value]
                        }

                        row[ extraIndex ] = toJson( map )
                    }
                    insertData(tableName, header, row)
                }
            }
        }
    }

    def createTable( tableName, columns ) 
    {
        try
        {
            db.updateRaw( "DROP TABLE ${tableName}" )
        }
        catch( def ignore ) {}
   
        def createTableQuery = """
            CREATE TABLE ${tableName} ( "___ID" BIGSERIAL,
            ${columns.collect { column -> column == "Extra" ? "${column} JSONB" : "${column} TEXT" }.join(", ")}
            )
        """
        db.updateRaw( createTableQuery )
    }

    def insertData(tableName, columns, row) 
    {
        def insertQuery = "INSERT INTO ${tableName} (${columns.join(", ")}) VALUES (${columns.collect { column -> column == 'Extra' ? "cast(? as jsonb)" : "?" }.join(", ")})"
        db.insertRaw( insertQuery, row )
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        def file = (Base64File) params.getValue("file")

        def tableName = null
        if( params.$type == "VEP" )
        {
            def parts = file.name.split( "\\." )
            def mainName = parts[ 0 ]
            tableName = mainName
            if( parts.length > 1 )
            {
                for( int i = 1; i < parts.length; i++ )
                {
                    tableName += "_" + parts[ i ]
                }
            }  
        }
         
        def uploadID = database.uploads << [
                fileName: file.name,
                tableName: tableName,       
                type : params.$type
        ]

        database.attachments << [
                fileName: file.name,
                mimeType: file.mimeTypes,
                data: file.data,
                ownerID: "uploads.$uploadID"
        ]

        if( params.$type == "VEP" )
        {
            processVCF( file, tableName )   
        } 
    }
}
