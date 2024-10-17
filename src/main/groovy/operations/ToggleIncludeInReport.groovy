package operations

import com.developmentontheedge.be5.server.operations.support.GOperationSupport

public class ToggleIncludeInReport extends GOperationSupport
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        db.update( """UPDATE snv_sample1 
                        SET includeInReport = CASE WHEN includeInReport = 'yes' THEN 'no' ELSE 'yes' END 
                      WHERE ID = ?""", 
            context.params.ID as Long )        

        redirectToTable( "snv_", "All records", [:] )
    }
}
