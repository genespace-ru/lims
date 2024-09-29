DELETE FROM resources;
DELETE FROM resource_recommendations;

 <#macro RESOURCE id, type, title, version, description, url, license, comment="null">
    INSERT INTO resources(id, type, title, version, description, url, license, comment) 
    VALUES( ${id}, ${type?str}, ${title?str}, ${version?str}, ${description?str}, ${url?str}, ${license?str}, ${comment});
</#macro>

<#macro RECOMMENDATION id, resource, recommendation, comment>
    INSERT INTO resource_recommendations(id, resource, recommendation, comment) 
    VALUES( ${id}, ${resource}, ${recommendation}, ${comment});
</#macro>



<@RESOURCE 1, "recommendation", "Руководство по интерпретации", "редакция 2024, версия 3",
"Руководства по интерпретации данных последовательности ДНК человека, полученных методами массового параллельного секвенирования (МПСMPS)."
"https://docs.google.com/document/d/1_tFCEzjARo3dx3cTWFa-4BPLgOpf8Lh7xYpr2ncp5bw", "" />

<@RESOURCE 2, "database", "GnomAD", " v3.1.2",
"Расширенная база данных геномных вариантов, основанная на базе Exome Aggregation Consortium."
"http://gnomad.broadinstitute.org/", "" />
<@RECOMMENDATION 1, 2, 1, "'Популяционные ресурсы'" />

<@RESOURCE 3, "database", "GnomAD", " v4.0.0",
"Расширенная база данных геномных вариантов, основанная на базе Exome Aggregation Consortium."
"http://gnomad.broadinstitute.org/", "" />
<@RECOMMENDATION 2, 3, 1, "'Популяционные ресурсы. Мы не рекомендуем использовать значения GnomAD v4.0.0, так как имеются сообщения об ошибках в частоте аллелей DOI: 10.1101/gr.277908.123'" />
