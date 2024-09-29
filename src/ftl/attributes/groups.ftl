DELETE FROM attribute_groups;

 <#macro GROUP id, order, title, title_ru, description, description_ru, comment="null">
    INSERT INTO attribute_groups(id, displayOrder, title, title_ru, description, description_ru, comment) 
    VALUES( ${id}, ${order}, ${title?str}, ${title_ru?str}, ${description?str}, ${description_ru?str}, ${comment});
</#macro>

<#-- Группы Genomenal 
1 - Common
2 - Gene
3 - ClinVar
4 - gnomAD 3
5 - gnomAD 4
6 - ExAC
7 - Other frequencies
8 - Conservation
9 - Protein function effect
10 - Protein function effect (additional)
11 - Other
12 - Custom annotation sources

https://genomenal.github.io/ngsw-docs-ru/results/main/snvs-indels/snv-details-panel
-->

<@GROUP 1, 1, "Common",  "Основная информация",
"General information about SNV or CNV.",
"Основная информация о варианте." /> 

<@GROUP 2, 2, "Gene",  "Ген",
"Main information about genes and transcripts."
"Oсновная информация о гене и о транскриптах." />
