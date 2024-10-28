DELETE FROM file_info fi WHERE fi.entity = 'workflow_info';
DELETE FROM workflow_info;

<#macro FILE id, suffix, name, path, size, entity, entityID, comment="null">
INSERT INTO file_info(id, project, fileType, fileName, path, size, entity, entityID, comment)
(SELECT ${id}, 1, ft.ID, ${name?str}, ${path?str}, ${size}, ${entity}, ${entityID}, ${comment}
 FROM file_types ft
 WHERE ft.suffix = ${suffix?str} );
</#macro>

<#macro WORKFLOW_INFO id, file_info, title, description, comment="null">
    INSERT INTO workflow_info(id, file_info, title, description, comment) 
    VALUES( ${id}, ${file_info}, ${title?str}, ${description?str}, ${comment});
</#macro>


<@FILE 100, ".nf", "fastqc.nf",
          "workflows/fastqc.nf", "1132", "'workflow_info'", 1 />
<@WORKFLOW_INFO 1, 100, "Quality control", 
"Контроль качества fastq ридов." />

