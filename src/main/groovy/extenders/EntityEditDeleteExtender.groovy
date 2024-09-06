package extenders

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.server.operations.support.OperationExtenderSupport

class EntityEditDeleteExtender extends OperationExtenderSupport
{
    @Override
    Object postGetParameters(Operation op, Object parameters, Map<String, Object> presetValues) throws Exception
    {
        if( userInfo.isAdmin() || userInfo.isUserInRole( "Manager" ) )
            return parameters

        for ( int i=0 ; i < op.context.records.length ; ++i )
        {
            def dps = database.getEntity( op.getInfo().getEntity().name ).get(op.context.records[i]) 
            if( userInfo.userName != dps.$whoInserted___ && userInfo.userName != dps.$whoModified___ )
            {
                op.setResult( OperationResult.error( "Для пользователей допускается только редактирование и удаление записей, созданных самим пользователем" ) )
            }
        }
        return parameters
    }
}