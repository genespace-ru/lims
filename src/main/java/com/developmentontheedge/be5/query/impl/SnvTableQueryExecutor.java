package com.developmentontheedge.be5.query.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.adapters.QRecParser;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryConstants;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.util.QueryUtils;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.model.AstStart;

/**
 * Executes the {@code snv_} "All records" view with a guard for the "no data yet"
 * case.
 *
 * <p>The view reads from a per-sample table whose name is built from the
 * {@code _tcloneid_} (sample ID) parameter, e.g. {@code snv_17}. Such a table is
 * created only when a VCF/VEP file has been loaded for the sample
 * (see {@code VCFLoader}/{@code LoaderSupport}). Until then the table does not
 * exist and the query fails with {@code relation "snv_<id>" does not exist}.
 *
 * <p>The view is declared with {@code type: Java} and {@code code} pointing at
 * this class; a Java query executor carries its SQL itself (the YAML {@code code}
 * field is the class name, not the SQL). This executor carries the SQL
 * ({@link #SQL}) and runs it through the standard be-sql pipeline. If the
 * database reports a missing relation (PostgreSQL SQLState 42P01) it returns an
 * empty result instead, so the UI shows the view's {@code messageWhenEmpty} text
 * rather than an error page. All other errors are rethrown unchanged.
 *
 * <p>Placed in {@code com.developmentontheedge.be5.query.impl} so it can use the
 * package-private {@link QuerySqlGenerator} / {@link CellFormatter} hooks the
 * same way {@link Be5SqlQueryExecutor} does.
 */
public class SnvTableQueryExecutor implements QueryExecutor
{
    /**
     * The "All records" view SQL — identical to the query that used to live in
     * snv_.yaml; the per-sample table is the {@code snv_<parameter:_tcloneid_ />}
     * in the FROM clause.
     */
    private static final String SQL =
            "        SELECT \n" +
            "            s.id AS \"___ID\",\n" +
            "          \n" +
            "            '<a href=\"#!form/snv_/All records/ToggleAttached/_tcloneid_=<parameter:_tcloneid_ />/ID=' || CAST(s.id AS CHAR) || '\">' ||\n" +
            "            '<img src=\"/static/images/pin_16_' || s.isAttached || '.svg\" title=\"Click to ' ||\n" +
            "            ( CASE WHEN s.isAttached='yes' THEN 'unpin the SNV.' ELSE 'pin the SNV to the top of the list.' END) || '\"></a>' ||\n" +
            "            \n" +
            "            '<a href=\"#!form/snv_/All records/ToggleHidden/_tcloneid_=<parameter:_tcloneid_ />/ID=' || CAST(s.id AS CHAR) || '\">' ||\n" +
            "            '<img src=\"/static/images/hide_16_' || s.isHidden || '.svg\" title=\"Click to ' ||\n" +
            "            ( CASE WHEN s.isHidden='yes' THEN 'show' ELSE 'hide' END) || ' the SNV in the list.\"></a>' ||\n" +
            "\n" +
            "            '<a href=\"#!form/snv_/All records/ToggleIncludeInReport/_tcloneid_=<parameter:_tcloneid_ />/ID=' || CAST(s.id AS CHAR) || '\">' ||\n" +
            "            '<img src=\"/static/images/list_16_' || s.includeInReport || '.svg\" title=\"Click to ' ||\n" +
            "            ( CASE WHEN s.includeInReport='yes' THEN 'exclude' ELSE 'include' END) || ' the SNV in the report.\"></a>'\n" +
            "              AS \"Actions\",\n" +
            "           '<css class=\"ws-nowrap\" />;<nosort/>' \n" +
            "              AS \";Actions\",\n" +
            "            \n" +
            "            s.vcf_chrom || ':' || s.vcf_pos AS \"Location\",\n" +
            "            s.vcf_ref || '>' || s.vcf_alt AS \"Replacement\",\n" +
            "            s.attributes->>'GT' AS \"Genotype\",\n" +
            "            COALESCE(s.attributes->>'DP' || ' (' || (s.attributes->>'AD') || ')', '') \"Read depth (Ref,Alt)\",\n" +
            "            CASE WHEN s.attributes->>'VAF' LIKE '%,%' THEN null\n" +
            "            ELSE ROUND(CAST(s.attributes->>'VAF' AS DECIMAL)*100, 0) END \n" +
            "              AS \"Variant allele fraction, %;<quick visible='true' />\",\n" +
            "            s.vcf_qual AS \"Quality;<quick visible='true' />\",\n" +
            "            s.vcf_filter AS \"Filter;<quick visible='true' />\",\n" +
            "            s.vcf_id AS \"IDs;<quick visible='false' />\",\n" +
            "            s.vcf_info AS \"Info;<quick visible='false' />\",\n" +
            "            \n" +
            "            (CASE WHEN s.attributes->>'Uploaded_variation' IS NOT null THEN 'VEP' ELSE '' END) \n" +
            "            AS \"Annotation;<quick visible='true' />\",\n" +
            "\n" +
            "            '<a href=\"#!table/snv_/SNV card/_tcloneid_=' || <parameter:_tcloneid_ /> || \n" +
            "            '/_snvid_=' || s.ID || '\">Card</>' AS \"Details\",\n" +
            "            \n" +
            "            s.attributes AS \"Attributes;<quick visible='false' />\",\n" +
            "            COALESCE('<a target=\"_blank\" href=\"/static/genomebrowser2.html#de=' || ss.setting_value || '&track=' || sss.setting_value || '&pos=' || SUBSTRING(s.vcf_chrom,4) || ':' || s.vcf_pos || '-' || s.vcf_pos+1 || '\">' ||'Open in GB'||'</a>', '')AS \"GB;<quick visible='true' />\"\n" +
            "        FROM snv_<parameter:_tcloneid_ /> s\n" +
            "        LEFT JOIN systemsettings ss ON ss.section_name='lims' AND ss.setting_name='sequence_path'\n" +
            "        LEFT JOIN systemsettings sss ON sss.section_name='lims' AND sss.setting_name='tracks'\n" +
            "        ORDER BY s.isAttached DESC, s.id\n" +
            "        ";

    private Meta meta;
    private DbService db;
    private QuerySession querySession;
    private UserInfoProvider userInfoProvider;
    private CellFormatter cellFormatter;
    private QuerySqlGenerator querySqlGenerator;

    private Query sqlQuery;
    private ContextApplier contextApplier;
    private Be5SqlQueryExecutor delegate;
    private java.util.Map<String, Object> parameters;
    private Boolean selectable;

    // The query-executor factory instantiates this class via Class.newInstance()
    // (no-arg) and then applies Guice member injection (injectMembers), so it
    // must have a no-arg constructor and use field injection.
    public SnvTableQueryExecutor()
    {
    }

    @Inject
    public void inject(Meta meta,
                       DbService db,
                       QuerySession querySession,
                       UserInfoProvider userInfoProvider,
                       QueryMetaHelper queryMetaHelper,
                       CellFormatter cellFormatter,
                       QuerySqlGenerator querySqlGenerator)
    {
        this.meta = meta;
        this.db = db;
        this.querySession = querySession;
        this.userInfoProvider = userInfoProvider;
        this.cellFormatter = cellFormatter;
        this.querySqlGenerator = querySqlGenerator;
        // A delegate that runs the standard pipeline; used for count() so the
        // count SQL is built exactly as Be5SqlQueryExecutor builds it.
        this.delegate = new Be5SqlQueryExecutor();
        this.delegate.inject(querySession, userInfoProvider, meta, db,
                queryMetaHelper, cellFormatter, querySqlGenerator);
    }

    public void initialize(Query viewQuery, java.util.Map<String, Object> parameters)
    {
        this.parameters = parameters;
        // Build a query that carries the SQL (a Java query's code field is the
        // executor class name, not the SQL). The cell css/nosort metadata is
        // carried on each cell by the be-sql AST (read via DynamicPropertyMeta in
        // CellFormatter), so it does not depend on the query's identity.
        this.sqlQuery = meta.createQueryFromSql(SQL);
        // Initialize the delegate with the SQL-carrying query so count() runs
        // through the exact standard pipeline (correct count SQL).
        this.delegate.initialize(this.sqlQuery, parameters);
        // selectable = view has 1- or 2-record operations (same rule as the
        // standard SQL executor).
        this.selectable = viewQuery.getEffectiveOperations().stream()
                .map(name -> meta.getOperation(viewQuery.getEntity().getName(), name))
                .filter(o -> meta.hasAccess(o.getRoles(), userInfoProvider.getCurrentRoles()))
                .map(o -> o.getRecords() == 1 || o.getRecords() == 2)
                .reduce(false, (a, b) -> a || b);
    }

    public List<QRec> execute()
    {
        AstStart sql;
        try
        {
            sql = querySqlGenerator.getSql(sqlQuery, parameters);
            contextApplier = new ContextApplier(new Be5QueryContext(sqlQuery, parameters,
                    querySession, userInfoProvider.getLoggedUser(), meta));
        }
        catch (RuntimeException e)
        {
            if (isMissingTableError(e))
                return Collections.emptyList();
            throw e;
        }

        List<QRec> rows;
        try
        {
            rows = db.list(sql, new QRecParser());
        }
        catch (RuntimeException e)
        {
            if (isMissingTableError(e))
                return Collections.emptyList();
            throw e;
        }
        return rows.stream()
                .map(row -> cellFormatter.toRow(row, name -> null, sqlQuery, contextApplier))
                .collect(Collectors.toList());
    }

    public long count()
    {
        AstStart sql;
        try
        {
            // Build the count AST on a limit-free copy of the query and wrap it as
            // COUNT(*) AS "count" FROM (query) AS "data" — the same transformation the
            // standard Be5SqlQueryExecutor.count() applies.
            sql = querySqlGenerator.getSql(sqlQuery, paramsWithoutLimit());
            QueryUtils.countFromQuery(sql.getQuery());
        }
        catch (RuntimeException e)
        {
            if (isMissingTableError(e))
                return 0L;
            throw e;
        }

        try
        {
            // Execute via the AstStart overload, which formats the AST directly
            // (no re-parse). The standard path goes through DbService.countFrom,
            // which re-parses the already-count-wrapped SQL string and fails on
            // this query's complex SELECT list (the "SELECT COUNT(1) FROM SELECT
            // COUNT(*) ..." ParseException). Passing the AstStart avoids that.
            // The count query has a single column (COUNT(*) AS "count"), so the
            // first value of the single result row is the count.
            List<QRec> rows = db.list(sql, new QRecParser());
            if (rows.isEmpty())
                return 0L;
            Object v = rows.get(0).getValue();
            return v instanceof Number ? ((Number) v).longValue() : 0L;
        }
        catch (RuntimeException e)
        {
            if (isMissingTableError(e))
                return 0L;
            throw e;
        }
    }

    /** A copy of the parameters without LIMIT/OFFSET/ORDER_COLUMN/ORDER_DIR. */
    private java.util.Map<String, Object> paramsWithoutLimit()
    {
        return new java.util.HashMap<String, Object>(parameters) {{
            remove(QueryConstants.LIMIT);
            remove(QueryConstants.OFFSET);
            remove(QueryConstants.ORDER_COLUMN);
            remove(QueryConstants.ORDER_DIR);
        }};
    }

    public int getOrderColumn() { return delegateInt(com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN, "-1"); }
    public int getOffset()      { return delegateInt(com.developmentontheedge.be5.query.QueryConstants.OFFSET, "0"); }
    public int getLimit()       { return delegateInt(com.developmentontheedge.be5.query.QueryConstants.LIMIT, Integer.toString(Integer.MAX_VALUE)); }
    public String getOrderDir() { return (String) parameters.getOrDefault(com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR, "asc"); }

    private int delegateInt(String key, String def)
    {
        return Integer.parseInt((String) parameters.getOrDefault(key, def));
    }

    public Boolean isSelectable() { return selectable; }
    public java.util.Map<String, Object> getParameters() { return parameters; }

    /**
     * Whether the exception chain contains a SQLException for a missing relation.
     */
    private static boolean isMissingTableError(Throwable t)
    {
        for (Throwable cause = t; cause != null; cause = cause.getCause())
        {
            if (cause instanceof SQLException)
            {
                String state = ((SQLException) cause).getSQLState();
                if ("42P01".equals(state))
                    return true;
            }
        }
        return false;
    }
}
