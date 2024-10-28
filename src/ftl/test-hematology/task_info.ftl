DELETE FROM task_info;

 <#macro TASK_INFO id, title, description, comment="null">
    INSERT INTO task_info(id, title, description, comment) 
    VALUES( ${id}, ${title?str}, ${description?str}, ${comment});
</#macro>


<@TASK_INFO 1, "Create project", "Созадает пустой проект." />

<@TASK_INFO 2, "Import Нанофор-СПС", "Импортирует данные секвенироания Нанофор-СПС." />

<@TASK_INFO 3, "Import fastq", "Загружает отдельный образец в формате fastq." />

<@TASK_INFO 4, "import VCF", "Загружает данные в VCFv4.2 формате." />

<@TASK_INFO 5, "import VEP", "Загружает данные в VEPv112 формате." />
