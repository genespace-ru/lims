package operations

import com.developmentontheedge.be5.server.operations.support.GOperationSupport

public class ToogleAttached extends GOperationSupport
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        super.invoke(parameters);

        redirectThisOperation();
    }
}
