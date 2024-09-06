DELETE FROM analyses;
<#macro analysis id, name, description>
INSERT INTO analyses(id, name, description, data, creationdate___, whoinserted___) 
            values(${id}, ${name?str}, ${description?str}, 'stub', current_timestamp, 'Administator');
</#macro>

<@analysis 1, "stub analyses", "Заглушка." />
