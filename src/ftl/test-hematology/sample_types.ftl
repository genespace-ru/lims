DELETE FROM sample_types;

<#macro TYPE id, title, description, template, comment="null">
INSERT INTO sample_types(id, title, description, template, comment) 
            values(${id}, ${title?str}, ${description?str}, ${template}, ${comment});
</#macro>

<@TYPE 1, "Кровь, онкология", "Кровь пациента.", 1 />
