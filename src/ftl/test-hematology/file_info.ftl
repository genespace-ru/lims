DELETE FROM file_info;

<#macro FILE id, suffix, name, path, size, entity, entityID, comment="null">
INSERT INTO file_info(id, project, fileType, fileName, path, size, entity, entityID, comment)
(SELECT ${id}, 1, ft.ID, ${name?str}, ${path?str}, ${size}, ${entity}, ${entityID}, ${comment}
 FROM file_types ft
 WHERE ft.suffix = ${suffix?str} );
</#macro>


<@FILE 1, "_r1.fastq.gz", "sample1_r1.fastq.gz",
          "samples/sample1_r1.fastq.gz", "1420706", "'samples'", 1 />
<@FILE 2, "_r2.fastq.gz", "sample1_r2.fastq.gz",
          "samples/sample1_r1.fastq.gz", "1055661 ", "'samples'", 1 />
<@FILE 3, "_r1.fastq.gz", "sample1_r1.fastq.gz",
          "samples/sample1_r1.fastq.gz", "1680507", "'samples'", 2 />
<@FILE 4, "_r2.fastq.gz", "sample1_r2.fastq.gz",
          "samples/sample1_r1.fastq.gz", "1237206", "'samples'", 2 />

<@FILE 5, "_r1_fastqc.html", "sample1_r1_fastqc.html",
          "results/fastqc/sample1_r1_fastqc.html", "233450", "'samples'", 1 />
<@FILE 6, "_r1_fastqc.zip", "sample1_r1_fastqc.zip",
          "results/fastqc/sample1_r1_fastqc.zip", "231489", "'samples'", 1 />
<@FILE 7, "_r2_fastqc.html", "sample1_r2_fastqc.html",
          "results/fastqc/sample1_r2_fastqc.html", "272311", "'samples'", 1 />
<@FILE 8, "_r2_fastqc.zip", "sample1_r2_fastqc.zip",
          "results/fastqc/sample1_r2_fastqc.zip", "309928", "'samples'", 1 />
          
<@FILE 9, "_r1_fastqc.html", "sample2_r1_fastqc.html",
          "results/fastqc/sample2_r1_fastqc.html", "232961", "'samples'", 2 />
<@FILE 10, "_r1_fastqc.zip", "sample2_r1_fastqc.zip",
          "results/fastqc/sample2_r1_fastqc.zip", "230790", "'samples'", 2 />
<@FILE 11, "_r2_fastqc.html", "sample2_r2_fastqc.html",
          "results/fastqc/sample2_r2_fastqc.html", "273190", "'samples'", 2 />
<@FILE 12, "_r2_fastqc.zip", "sample2_r2_fastqc.zip",
          "results/fastqc/sample2_r2_fastqc.zip", "310454", "'samples'", 2 />

<@FILE 13, "multiqc_report.html", "multiqc_report.html",
          "results/multiqc/multiqc_report.html", "4744636", "'projects'", 1 />
<@FILE 14, "multiqc_fastqc.txt", "multiqc_fastqc.txt",
          "results/multiqc/multiqc_data/multiqc_fastqc.txt ", "1117", "'projects'", 1 />

<@FILE 15, ".vcf", "sample1.vcf",
           "results/VCF/sample1.vcf", "28696", "'samples'", 1 /> 
<@FILE 16, ".vcf", "sample2.vcf",
           "results/VCF/sample2.vcf", "41703", "'samples'", 2 /> 

<@FILE 17, ".vep", "sample1.vep",
           "results/VEP/sample1.vcf", "1493969", "'samples'", 1 /> 
<@FILE 18, ".vep", "sample2.vep",
           "results/VEP/sample2.vcf", "1867026", "'samples'", 2 /> 



