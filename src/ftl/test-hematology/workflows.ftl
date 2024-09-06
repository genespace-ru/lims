DELETE FROM workflows;
<#macro workflow id, name, description>
INSERT INTO workflows(id, name, description, data, creationdate___, whoinserted___) 
            values(${id}, ${name?str}, ${description?str}, 'stub', current_timestamp, 'Administator');
</#macro>

<@workflow 1, "stub workflow", "Заглушка." />
