DELETE FROM sample_templates;
<#macro sample_template id, name, description>
INSERT INTO sample_templates(id, name, description, data, creationdate___, whoinserted___) 
            values(${id}, ${name?str}, ${description?str}, 'stub', current_timestamp, 'Administator');
</#macro>

<@sample_template 1, "stub template", "Заглушка." />
