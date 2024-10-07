DELETE FROM file_types;

<#macro TYPE id, suffix, description, comment="null">
INSERT INTO file_types(id, suffix, description, comment) 
            values(${id}, ${suffix?str}, ${description?str}, ${comment});
</#macro>

<@TYPE 1, ".fastq", "Данные секвенирования в формате fastq." />
<@TYPE 2, ".fastq.gz", "Данные секвенирования в формате fastq, архивированные." />
<@TYPE 3, "_r1.fastq.gz", "Данные секвенирования в формате fastq, архивированные, прямые прочтения." />
<@TYPE 4, "_r2.fastq.gz", "Данные секвенирования в формате fastq, архивированные, обратные прочтения." />

<@TYPE 5, "_fastqc.html", "Отчет по контролю качества, главный html файл." />
<@TYPE 6, "_fastqc.zip", "Отчет по контролю качества, архив  html файлов." />

<@TYPE 7, "_r1_fastqc.html", "Отчет по контролю качества, главный html файл, прямые прочтения." />
<@TYPE 8, "_r2_fastqc.html", "Отчет по контролю качества, главный html файл, обратные прочтения." />
<@TYPE 9, "_r1_fastqc.zip", "Отчет по контролю качества, архив  html файлов, прямые прочтения." />
<@TYPE 10, "_r2_fastqc.zip", "Отчет по контролю качества, архив  html файлов, обратные прочтения." />

<@TYPE 20, "multiqc_report.html", "Отчет по контролю качества для проекта целиком." />
<@TYPE 21, "multiqc_fastqc.txt", "Отчет по контролю качества для проекта целиком, в csv формате, для импорта." />

<@TYPE 30, ".vcf", "Данные генотипирования." />
<@TYPE 31, ".vep", "Данные аннотирования SNV программой VEP." />
