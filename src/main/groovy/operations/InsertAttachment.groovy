package operations

import com.developmentontheedge.be5.databasemodel.util.DpsUtils
import com.developmentontheedge.be5.server.model.Base64File
import com.developmentontheedge.be5.server.operations.support.GOperationSupport

class InsertAttachment extends GOperationSupport
{
    Map<String, Object> presets

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        presets = presetValues
        params.file = [ TYPE: Base64File, DISPLAY_NAME: "File name" ]
        return DpsUtils.setValues(params, presetValues)
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        def file = (Base64File) params.getValue("file")
        database.attachments << [
                fileName  : file.name,
                mimeType : file.mimeTypes,
                data      : file.data,
                ownerID  : presets.ownerID
        ]
    }
}
