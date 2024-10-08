<#macro COMMON_WHO_FIELDS alias>
  TO_CHAR(${alias}.creationDate___, 'DD.MM.YYYY HH24:MI') || ', ' || ${alias}.whoInserted___
     AS "Created;<quick visible='false' />",
  TO_CHAR(${alias}.modificationDate___, 'DD.MM.YYYY HH24:MI') || ', ' || ${alias}.whoModified___
     AS "Modified;<quick visible='false' />"
</#macro>

<#macro DICTIONARY_REF table, title, description>
    SELECT
        '<a href="./#!table/${table}/All records">${title}</a>' AS "Справочник",
                ${description?str} AS "Описание"
</#macro>
