package operations

import com.developmentontheedge.be5.server.operations.FilterOperation
import com.developmentontheedge.be5.util.FilterUtil
import com.developmentontheedge.beans.DynamicPropertySet

import static com.developmentontheedge.be5.server.FrontendActions.closeMainModal
import static com.developmentontheedge.be5.server.FrontendActions.updateParentDocument

/**
 * Custom Filter for ForProjectCard queries.
 *
 * The built-in FilterOperation uses FilterUtil.getContextParams() which strips
 * all _-prefixed params from the request — so ___prjID is lost and the query
 * gets an empty project filter, producing a SQL syntax error.
 *
 * This override injects ___prjID back from the original request context so
 * the project filter is preserved when the user applies a file-type filter.
 */
public class ProjectCardFilterOperation extends FilterOperation {

    @Override
    public void invoke(Object parameters) throws Exception {
        // Get the standard filter params (with _-prefixed keys stripped)
        Map<String, Object> params = dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters)

        // Re-inject ___prjID from the request context (it's in context.getParams()
        // but was stripped by FilterUtil.getContextParams in the parent)
        def prjId = context.getParams().get("___prjID")
        if (prjId != null && !prjId.toString().isBlank()) {
            params.put("___prjID", prjId.toString())
        }

        // Merge remaining context params (non-_-prefixed)
        params.putAll(FilterUtil.getContextParams(context.getParams()))

        setResultFinished(
                updateParentDocument(documentGenerator.getDocument(getQuery(), params)),
                closeMainModal()
        )
    }
}
