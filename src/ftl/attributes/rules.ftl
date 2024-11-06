DELETE FROM rule_sets;

<#macro ROOL_SET id, recommendation, title, version, description, comment="null">
    INSERT INTO rule_sets(id, recommendation, title, version, description, comment) 
    VALUES( ${id}, ${recommendation}, ${title?str}, ${version?str}, ${description?str}, ${comment});
</#macro>

<#-- Рекомендации -->
<#assign ACMG = 1>

<@ROOL_SET 1, ACMG, "Рекомендации по интерпретации", "0.1", 
"Руководства по интерпретации данных последовательности ДНК человека, полученных методами массового параллельного секвенирования (МПСMPS)." />
