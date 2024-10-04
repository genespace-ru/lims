DELETE FROM file_info;

<#macro FILE id, suffix, name, path, size, entity, entityID, comment="null">
INSERT INTO file_info(id, project, fileType, fileName, path, size, entity, entityID, comment)
(SELECT ${id}, 1, ft.ID, ${name?str}, ${path?str}, ${size}, ${entity}, ${entityID}, ${comment}
 FROM file_types ft
 WHERE ft.suffix = ${suffix?str} );
</#macro>


<@FILE 1, "_r1.fastq.gz", "sample1_r1.fastq.gz",
          "samples/sample1_r1.fastq.gz", "1420706", "'samples'", 1 />
