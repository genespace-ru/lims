DELETE FROM attribute_types;

 <#macro TYPE id, title, description>
    INSERT INTO attribute_types(id, title, description) VALUES( ${id}, ${title?str}, ${description?str});
</#macro>

 <@TYPE 1, "INT",    "Целое число." /> 
 <@TYPE 2, "DOUBLE", "Вещественное число." /> 
 <@TYPE 3, "STRING", "Строка, произвольной длины." /> 