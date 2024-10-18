DELETE FROM task_info;

 <#macro TASK_INFO id, title, description, comment="null">
    INSERT INTO task_info(id, title, description, comment) 
    VALUES( ${id}, ${title?str}, ${description?str}, ${comment});
</#macro>


<@TASK_INFO 1, "import VCF", "Загружает данные в VCFv4.2 формате." />

<@TASK_INFO 2, "import VEP", "Загружает данные в VEPv112 формате." />
