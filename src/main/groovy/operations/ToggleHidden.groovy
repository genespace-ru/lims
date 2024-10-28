package operations

import com.developmentontheedge.be5.server.operations.support.GOperationSupport

public class ToggleHidden extends GOperationSupport
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        db.update( """UPDATE snv_${ context.params._tcloneid_ ? context.params._tcloneid_ : "" }
                        SET isHidden = CASE WHEN isHidden = 'yes' THEN 'no' ELSE 'yes' END 
                      WHERE ID = ?""", 
            context.params.ID as Long )        

        if( context.params._tcloneid_  )
        {
            redirectToTable( "snv_", "All records", [ "_tcloneid_": context.params._tcloneid_ ] )
        }
        else
        {
            redirectToTable( "snv_", "All records", [:] )
        }
    }
}