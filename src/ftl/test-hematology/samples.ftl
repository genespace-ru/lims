DELETE FROM samples;
DELETE FROM run_samples;

<#macro SAMPLE id, project_id, type, title, 
        plate, well, index_plate_well, I7_index_id, index1, I5_index_id, index2,
        description="null", comment="null">
INSERT INTO samples(id, project, typeID, title, description,comment) 
VALUES(${id}, ${project_id}, ${type}, ${title?str}, ${description}, ${comment});

INSERT INTO run_samples(id, run_id, sample_id, 
  plate, well, index_plate_well, I7_index_id, index1, I5_index_id, index2)  
VALUES(${id}, 1, ${id},  
  ${plate}, ${well?str}, ${index_plate_well?str}, ${I7_index_id?str}, ${index1?str}, ${I5_index_id?str}, ${index2?str} ); 
</#macro>

<@SAMPLE 1, 1, 1, "sample1",
"null", "A01", "A01", "i7_UDI01", "GTAACATC", "i5_UDI01", "CAGCGATT" />

<@SAMPLE 2, 1, 1, "sample2",
"null", "A02", "A02", "i7_UDI09", "ATTATCAA", "i5_UDI09", "CGACTCTC" />
