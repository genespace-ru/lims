package operations.api

import com.developmentontheedge.be5.meta.Meta

import com.developmentontheedge.be5.operation.TransactionalOperation
import com.developmentontheedge.be5.server.operations.support.OperationSupport

import com.developmentontheedge.beans.DynamicPropertySet as DPS
import com.developmentontheedge.beans.DynamicPropertySetSupport

import javax.inject.Inject

import static groovy.json.JsonOutput.toJson

class get extends OperationSupport /*implements TransactionalOperation*/
{
    @Inject private Meta meta

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DPS params = new DynamicPropertySetSupport()
        params.json = [ value: presetValues.json ]
        return params
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        DPS params = parameters as DPS ?: new DynamicPropertySetSupport()

        def slurper = new groovy.json.JsonSlurper()
        def json = slurper.parseText( params.$json )
        System.err.println( "Parsed JSON: " + json )

        def result = [:]
        result.status = "success"
        result.work = "get"
          
        setResultFinished( toJson( result ) )
    }
}