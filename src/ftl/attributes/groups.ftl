DELETE FROM attribute_groups;

 <#macro GROUP id, order, title, title_ru, description, description_ru, comment="null">
    INSERT INTO attribute_groups(id, displayOrder, title, title_ru, description, description_ru, comment) 
    VALUES( ${id}, ${order}, ${title?str}, ${title_ru?str}, ${description?str}, ${description_ru?str}, ${comment});
</#macro>

<#-- Группы Genomenal 
1 - Common
2 - Gene
3 - ClinVar
4 - gnomAD 3``
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

<@GROUP 3, 3, "ClinVar",  "ClinVar",
"Information about the phenotypic significance of the haplotype and variant from ClinVar.",
"Информация о фенотипической значимости гаплотипа и варианта из базы данных ClinVar." />

<@GROUP 4, 4, "gnomAD 3",  "gnomAD 3",
"Allele frequency information from gnomAD v3.",
"Информация об аллельных частотах из базы данных gnomAD (версия 3)." />

<@GROUP 5, 5, "gnomAD 4",  "gnomAD 4",
"Allele frequency information from gnomAD v4.",
"Информация об аллельных частотах из базы данных gnomAD (версия 4)." />

<@GROUP 6, 6, "ExAC",  "ExAC",
"Allele frequency information from ExAC.",
"Информация об аллельных частотах из базы данных ExAC." />

<@GROUP 7, 7, "Other frequencies",  "Другие частоты",
"Allele frequency information from 1000G, UK10K, and ESP.",
"Информация об аллельных частотах из проектов 1000 Genomes, UK10K и ESP." />

<@GROUP 8, 8, "Conservation",  "Консервативность",
"Information about the conservation of the variant.",
"Информация о консервативности варианта." />

<@GROUP 9, 9, "Protein function effect",  "Эффект на функцию белка",
"Predictions of the effect of amino acid substitutions on protein function.",
"Результаты предсказания эффекта замены аминокислот на функцию белка." />

<@GROUP 10, 10, "Protein function effect (additional)",  "Эффект на функцию белка (дополнительно)",
"Additional predictions of the effect of amino acid substitutions on protein function.",
"Дополнительные результаты предсказания эффекта замены аминокислот на функцию белка." />

<@GROUP 11, 11, "Other",  "Дополнительно",
"Additional information about the variant.",
"Дополнительная информация о варианте." />

<@GROUP 12, 12, "Custom annotation sources",  "Пользовательские аннотации",
"Custom annotations added to the variant.",
"Пользовательские аннотации, добавленные к варианту." />