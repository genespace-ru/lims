DELETE FROM attributes;
DELETE FROM attribute_resources;

 <#macro ATTRIBUTE id, group, order, title, title_ru, description, description_ru, 
   level, storage, type, comment="null", dictionary="null", rule="null", displayIfEmpty="'no'", urlMask="null" >  
    INSERT INTO attributes(id, entity, groupID, displayOrder, title, title_ru, description, description_ru, 
       level, storage, type, comment, dictionary, rule, displayIfEmpty, urlMask)
    VALUES( ${id}, 'snv_',  ${group}, ${order}, ${title?str}, ${title_ru?str}, ${description?str}, ${description_ru?str}, 
    ${level?str}, ${storage?str}, ${type}, ${comment}, ${dictionary}, ${rule}, ${displayIfEmpty}, ${urlMask} );
</#macro>

<#macro RESOURCE id, attribute, resource, comment="null">
    INSERT INTO attribute_resources(id, attribute, resource, comment)
    VALUES(${id}, ${attribute}, ${resource}, ${comment});
</#macro>

<#assign GR_COMMON      = 1>
<#assign GR_GENE        = 2>
<#assign GR_ClinVar     = 3>
<#assign GR_gnomAD_3    = 4>
<#assign GR_gnomAD_4    = 5>
<#assign GR_ExAC        = 6>
<#assign GR_Other_freq  = 7>
<#assign GR_Conservation= 8>
<#assign GR_Protein     = 9>
<#assign GR_Protein_add = 10>
<#assign GR_Other       = 11>
<#assign GR_Custom      = 12>

<#assign INT    = 1 /> 
<#assign DOUBLE = 2 /> 
<#assign STRING = 3 /> 

<#assign VEP = 57>

<@ATTRIBUTE 101, GR_COMMON, 1, "Uploaded_variation", "Загруженный вариант", 
"Identifier of uploaded variant", 
"Идентификатор загруженного варианта",   
"SNV", "query", INT />   
<@RESOURCE 57, 1, VEP />

<@ATTRIBUTE 102, GR_COMMON, 2, "Location", "Локация", 
"Location of variant in standard coordinate format (chr:start or chr:start-end)", 
"Местоположение варианта в стандартном координатном формате (chr:start или chr:start-end)", 
"SNV", "query", STRING />   
<@RESOURCE 57, 1, VEP />

<@ATTRIBUTE 103, GR_COMMON, 3, "Allele", "Аллель", 
"The variant allele used to calculate the consequence", 
"Аллель варианта, использованный для расчета последствия", 
"SNV", "query", STRING />   
<@RESOURCE 57, 1, VEP />

<@ATTRIBUTE 104, GR_COMMON, 4, "Consequence", "Последствие", 
"Consequence type", 
"Тип последствия", 
"SNV", "query", STRING />   
<@RESOURCE 57, 1, VEP />