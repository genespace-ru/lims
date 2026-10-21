DELETE FROM analyses;
<#macro analysis id, project, name, description>
INSERT INTO analyses(id, project, name, description, data, creationdate___, whoinserted___)
            values(${id}, ${project}, ${name?str}, ${description?str}, 'stub', current_timestamp, 'Administator');
</#macro>

<@analysis 1, 1, "stub analyses", "Заглушка." />
