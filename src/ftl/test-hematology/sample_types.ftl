DELETE FROM sample_types;

<#macro TYPE id, title, description, template, comment="null">
INSERT INTO sample_types(id, title, description, template, comment) 
            values(${id}, ${title?str}, ${description?str}, ${template}, ${comment});
</#macro>

<@TYPE 1, "Oncopanel 1", "Онкопанель для тагетного секвенирования.", 1 />
