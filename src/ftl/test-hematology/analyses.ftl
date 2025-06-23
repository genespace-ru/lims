DELETE FROM analyses;
<#macro analysis id, project_id, name, description>
INSERT INTO analyses(id, project_id, name, description, data, creationdate___, whoinserted___) 
            values(${id}, ${project_id}, ${name?str}, ${description?str}, 'stub', current_timestamp, 'Administator');
</#macro>

<@analysis 1, 1, "stub analyses", "Заглушка." />
