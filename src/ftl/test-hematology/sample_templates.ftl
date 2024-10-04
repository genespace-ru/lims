DELETE FROM sample_templates;

<#macro TEMPLATE id, title, description, comment="null">
INSERT INTO sample_templates(id, title, description, comment, data) 
            values(${id}, ${title?str}, ${description?str}, ${comment}, 'заглушка');
</#macro>

<@TEMPLATE 1, "Кровь, онкология", "Шаблон для описания свойств образца онкоданных (заглушка)." />
