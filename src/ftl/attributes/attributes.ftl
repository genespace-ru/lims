DELETE FROM attributes;
DELETE FROM attribute_resources;

<#assign VEP = 57>
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

<#macro ATTRIBUTE_VEP id, group, order, title, title_ru, description, description_ru, 
  level, storage, type, comment="null", dictionary="null", rule="null", displayIfEmpty="'no'", urlMask="null" >  
    INSERT INTO attributes(id, entity, groupID, displayOrder, title, title_ru, description, description_ru, 
       level, storage, type, comment, dictionary, rule, displayIfEmpty, urlMask)
    VALUES( ${id}, 'snv_',  ${group}, ${order}, ${title?str}, ${title_ru?str}, ${description?str}, ${description_ru?str}, 
    ${level?str}, ${storage?str}, ${type}, ${comment}, ${dictionary}, ${rule}, ${displayIfEmpty}, ${urlMask} );

    INSERT INTO attribute_resources(id, attribute, resource)
    VALUES(${id}0, ${id}, ${VEP});
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

<@ATTRIBUTE_VEP 101, GR_COMMON, 1, "Uploaded_variation", "Загруженный вариант", 
"Identifier of uploaded variant", 
"Идентификатор загруженного варианта",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 102, GR_COMMON, 2, "Location", "Локация", 
"Location of variant in standard coordinate format (chr:start or chr:start-end)", 
"Местоположение варианта в стандартном координатном формате (chr:start или chr:start-end)", 
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP 103, GR_COMMON, 3, "Allele", "Аллель", 
"The variant allele used to calculate the consequence", 
"Аллель варианта, использованный для расчета последствий",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 104, GR_COMMON, 4, "Existing_variation", "Существующий вариант", 
"Identifier(s) of co-located known variants", 
"Идентификатор(ы) известных вариантов, расположенных в той же позиции",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 105, GR_COMMON, 5, "VARIANT_CLASS", "Класс варианта", 
"SO variant class", 
"Класс варианта по SO",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 106, GR_COMMON, 6, "IMPACT", "Влияние", 
"Subjective impact classification of consequence type", 
"Субъективная классификация влияния типа последствий",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 107, GR_COMMON, 7, "DISTANCE", "Дистанция", 
"Shortest distance from variant to transcript", 
"Кратчайшее расстояние от варианта до транскрипта",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 108, GR_COMMON, 8, "STRAND", "Стренд", 
"Strand of the feature (1/-1)", 
"Направление стренда признака (1/-1)",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 109, GR_COMMON, 9, "FLAGS", "Флаги", 
"Transcript quality flags", 
"Флаги качества транскрипта",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 201, GR_GENE, 1, "Gene", "Ген", 
"Stable ID of affected gene", 
"Устойчивый идентификатор затронутого гена",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 202, GR_GENE, 2, "Feature", "Функция", 
"Stable ID of feature", 
"Устойчивый идентификатор функции",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 203, GR_GENE, 3, "Feature_type", "Тип функции", 
"Type of feature - Transcript, RegulatoryFeature or MotifFeature", 
"Тип функции - Транскрипт, Регуляторная функция или Мотивная функция",   
"transcript", "query", INT />   

<@ATTRIBUTE_VEP 204, GR_GENE, 4, "Consequence", "Последствие", 
"Consequence type", 
"Тип последствия",   
"transcript", "query", INT />   

<@ATTRIBUTE_VEP 205, GR_GENE, 5, "cDNA_position", "Позиция cDNA", 
"Relative position of base pair in cDNA sequence", 
"Относительная позиция пары оснований в последовательности cDNA",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 206, GR_GENE, 6, "CDS_position", "Позиция CDS", 
"Relative position of base pair in coding sequence", 
"Относительная позиция пары оснований в кодирующей последовательности",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 207, GR_GENE, 7, "Protein_position", "Позиция белка", 
"Relative position of amino acid in protein", 
"Относительная позиция аминокислоты в белке",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 208, GR_GENE, 8, "Amino_acids", "Аминокислоты", 
"Reference and variant amino acids", 
"Справочные и измененные аминокислоты",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 209, GR_GENE, 9, "Codons", "Кодоны", 
"Reference and variant codon sequence", 
"Справочная и измененная последовательность кодонов",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 210, GR_GENE, 10, "Extra", "Дополнительно", 
"Additional gene-related attributes", 
"Дополнительные атрибуты, связанные с геном",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 211, GR_GENE, 11, "IMPACT", "Влияние", 
"Subjective impact classification of consequence type", 
"Субъективная классификация влияния типа последствий",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 212, GR_GENE, 12, "HGVSc", "HGVS cDNA", 
"HGVS coding sequence name", 
"Имя последовательности HGVS cDNA",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 213, GR_GENE, 13, "HGVSp", "HGVS protein", 
"HGVS protein sequence name", 
"Имя последовательности HGVS белка",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP 214, GR_GENE, 14, "HGVS_OFFSET", "Смещение HGVS", 
"Indicates by how many bases the HGVS notations for this variant have been shifted", 
"Указывает, на сколько оснований смещены обозначения HGVS для этого варианта",   
"transcript", "query", INT />   

<@ATTRIBUTE_VEP 301, GR_ClinVar, 1, "ClinVar_CLNSIG", "Клиническая значимость", 
"ClinVar clinical significance of the dbSNP variant", 
"Клиническая значимость варианта в ClinVar", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 302, GR_ClinVar, 2, "ClinVar_CLNREVSTAT", "Ревизионный статус", 
"CLNREVSTAT field from clinvar.vcf.gz", 
"Поле CLNREVSTAT из файла clinvar.vcf.gz", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 303, GR_ClinVar, 3, "ClinVar_CLNDN", "Заболевание", 
"CLNDN field from clinvar.vcf.gz", 
"Поле CLNDN из файла clinvar.vcf.gz", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 401, GR_gnomAD_3, 1, "gnomADe_AF", "Частота gnomAD exomes", 
"Frequency of existing variant in gnomAD exomes combined population", 
"Частота существующего варианта в объединенной популяции exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 402, GR_gnomAD_3, 2, "gnomADe_AFR_AF", "Частота gnomAD exomes Африка", 
"Frequency of existing variant in gnomAD exomes African/American population", 
"Частота существующего варианта в африканской/американской популяции exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 403, GR_gnomAD_3, 3, "gnomADe_AMR_AF", "Частота gnomAD exomes Америка", 
"Frequency of existing variant in gnomAD exomes American population", 
"Частота существующего варианта в американской популяции exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 404, GR_gnomAD_3, 4, "gnomADe_ASJ_AF", "Частота gnomAD exomes ашкенази", 
"Frequency of existing variant in gnomAD exomes Ashkenazi Jewish population", 
"Частота существующего варианта в ашкеназской еврейской популяции exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 405, GR_gnomAD_3, 5, "gnomADe_EAS_AF", "Частота gnomAD exomes Восточная Азия", 
"Frequency of existing variant in gnomAD exomes East Asian population", 
"Частота существующего варианта в популяции Восточной Азии exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 406, GR_gnomAD_3, 6, "gnomADe_FIN_AF", "Частота gnomAD exomes Финляндия", 
"Frequency of existing variant in gnomAD exomes Finnish population", 
"Частота существующего варианта в финской популяции exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 407, GR_gnomAD_3, 7, "gnomADe_NFE_AF", "Частота gnomAD exomes Северная Европа", 
"Frequency of existing variant in gnomAD exomes Non-Finnish European population", 
"Частота существующего варианта в популяции Северной Европы (не-Финляндия) exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 408, GR_gnomAD_3, 8, "gnomADe_OTH_AF", "Частота gnomAD exomes другие", 
"Frequency of existing variant in gnomAD exomes Other populations", 
"Частота существующего варианта в других популяциях exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 409, GR_gnomAD_3, 9, "gnomADe_SAS_AF", "Частота gnomAD exomes Южная Азия", 
"Frequency of existing variant in gnomAD exomes South Asian population", 
"Частота существующего варианта в популяции Южной Азии exomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 501, GR_gnomAD_4, 1, "gnomADg_AF", "Частота gnomAD genomes", 
"Frequency of existing variant in gnomAD genomes combined population", 
"Частота существующего варианта в объединенной популяции genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 502, GR_gnomAD_4, 2, "gnomADg_AFR_AF", "Частота gnomAD genomes Африка", 
"Frequency of existing variant in gnomAD genomes African/American population", 
"Частота существующего варианта в африканской/американской популяции genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 503, GR_gnomAD_4, 3, "gnomADg_AMR_AF", "Частота gnomAD genomes Америка", 
"Frequency of existing variant in gnomAD genomes American population", 
"Частота существующего варианта в американской популяции genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 504, GR_gnomAD_4, 4, "gnomADg_EAS_AF", "Частота gnomAD genomes Восточная Азия", 
"Frequency of existing variant in gnomAD genomes East Asian population", 
"Частота существующего варианта в восточноазиатской популяции genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 505, GR_gnomAD_4, 5, "gnomADg_FIN_AF", "Частота gnomAD genomes Финляндия", 
"Frequency of existing variant in gnomAD genomes Finnish population", 
"Частота существующего варианта в финской популяции genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 506, GR_gnomAD_4, 6, "gnomADg_NFE_AF", "Частота gnomAD genomes Европа", 
"Frequency of existing variant in gnomAD genomes non-Finnish European population", 
"Частота существующего варианта в нефинской европейской популяции genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 507, GR_gnomAD_4, 7, "gnomADg_OTH_AF", "Частота gnomAD genomes Другое", 
"Frequency of existing variant in gnomAD genomes other populations", 
"Частота существующего варианта в других популяциях genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 508, GR_gnomAD_4, 8, "gnomADg_SAS_AF", "Частота gnomAD genomes Южная Азия", 
"Frequency of existing variant in gnomAD genomes South Asian population", 
"Частота существующего варианта в южноазиатской популяции genomes gnomAD", 
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 601, GR_ExAC, 1, "MAX_AF", "Максимальная частота аллеля", 
"Maximum observed allele frequency in 1000 Genomes, ESP and ExAC/gnomAD", 
"Максимально наблюдаемая частота аллеля в 1000 Genomes, ESP и ExAC/gnomAD",   
"transcript", "query", DOUBLE />   

<@ATTRIBUTE_VEP 602, GR_ExAC, 2, "MAX_AF_POPS", "Популяции с максимальной частотой", 
"Populations in which maximum allele frequency was observed", 
"Популяции, в которых была зафиксирована максимальная частота аллеля",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 701, GR_Other_freq, 1, "AF", "Частота аллеля", 
"Frequency of existing variant in 1000 Genomes combined population", 
"Частота существующего варианта в объединенной популяции 1000 Геномов", 
"SNV", "query", DOUBLE />   

<@ATTRIBUTE_VEP 702, GR_Other_freq, 2, "AFR_AF", "Частота аллеля в Африканской популяции", 
"Frequency of existing variant in 1000 Genomes combined African population", 
"Частота существующего варианта в объединенной Африканской популяции 1000 Геномов", 
"SNV", "query", DOUBLE />   

<@ATTRIBUTE_VEP 703, GR_Other_freq, 3, "AMR_AF", "Частота аллеля в Американской популяции", 
"Frequency of existing variant in 1000 Genomes combined American population", 
"Частота существующего варианта в объединенной Американской популяции 1000 Геномов", 
"SNV", "query", DOUBLE />   

<@ATTRIBUTE_VEP 704, GR_Other_freq, 4, "EAS_AF", "Частота аллеля в Восточно-Азиатской популяции", 
"Frequency of existing variant in 1000 Genomes combined East Asian population", 
"Частота существующего варианта в объединенной Восточно-Азиатской популяции 1000 Геномов", 
"SNV", "query", DOUBLE />   

<@ATTRIBUTE_VEP 705, GR_Other_freq, 5, "EUR_AF", "Частота аллеля в Европейской популяции", 
"Frequency of existing variant in 1000 Genomes combined European population", 
"Частота существующего варианта в объединенной Европейской популяции 1000 Геномов", 
"SNV", "query", DOUBLE />   

<@ATTRIBUTE_VEP 706, GR_Other_freq, 6, "SAS_AF", "Частота аллеля в Южно-Азиатской популяции", 
"Frequency of existing variant in 1000 Genomes combined South Asian population", 
"Частота существующего варианта в объединенной Южно-Азиатской популяции 1000 Геномов", 
"SNV", "query", DOUBLE />   

<@ATTRIBUTE_VEP 901, GR_Protein, 1, "BIOTYPE", "Тип транскрипта", 
"Biotype of transcript or regulatory feature", 
"Тип транскрипта или регуляторной особенности", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 902, GR_Protein, 2, "CANONICAL", "Канонический транскрипт", 
"Indicates if transcript is canonical for this gene", 
"Указывает, является ли транскрипт каноническим для этого гена", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 903, GR_Protein, 3, "MANE_SELECT", "Транскрипт MANE Select", 
"MANE Select (Matched Annotation from NCBI and EMBL-EBI) Transcript", 
"Транскрипт MANE Select (Сопоставленная аннотация от NCBI и EMBL-EBI)", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 904, GR_Protein, 4, "MANE_PLUS_CLINICAL", "Транскрипт MANE Plus Clinical", 
"MANE Plus Clinical (Matched Annotation from NCBI and EMBL-EBI) Transcript", 
"Транскрипт MANE Plus Clinical (Сопоставленная аннотация от NCBI и EMBL-EBI)", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 905, GR_Protein, 5, "TSL", "Уровень поддержки транскрипта", 
"Transcript support level", 
"Уровень поддержки транскрипта", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 906, GR_Protein, 6, "PHENO", "Фенотип", 
"Indicates if existing variant(s) is associated with a phenotype, disease or trait; multiple values correspond to multiple variants", 
"Указывает, связаны ли существующие варианты с фенотипом, заболеванием или признаком; несколько значений соответствуют нескольким вариантам", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 907, GR_Protein, 7, "PUBMED", "ID публикации PubMed", 
"Pubmed ID(s) of publications that cite existing variant", 
"ID публикаций PubMed, которые ссылаются на существующий вариант", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 901, GR_Protein, 8, "APPRIS", "APPRIS", 
"Annotates alternatively spliced transcripts as primary or alternate based on a range of computational methods", 
"Аннотации альтернативно спliced транскриптов как первичных или альтернативных на основе ряда вычислительных методов",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 902, GR_Protein, 9, "CCDS", "CCDS", 
"Indicates if transcript is a CCDS transcript", 
"Указывает, является ли транскрипт транскриптом CCDS",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 903, GR_Protein, 10, "ENSP", "Protein identifier", 
"Protein identifier", 
"Идентификатор белка",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 904, GR_Protein, 11, "SWISSPROT", "UniProtKB/Swiss-Prot accession", 
"UniProtKB/Swiss-Prot accession", 
"Идентификатор UniProtKB/Swiss-Prot",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 905, GR_Protein, 12, "TREMBL", "UniProtKB/TrEMBL accession", 
"UniProtKB/TrEMBL accession", 
"Идентификатор UniProtKB/TrEMBL",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 906, GR_Protein, 13, "UNIPARC", "UniParc accession", 
"UniParc accession", 
"Идентификатор UniParc",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 907, GR_Protein, 14, "UNIPROT_ISOFORM", "Direct mappings to UniProtKB isoforms", 
"Direct mappings to UniProtKB isoforms", 
"Прямые сопоставления с изоформами UniProtKB",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 908, GR_Protein, 15, "SOURCE", "Source of transcript", 
"Source of transcript", 
"Источник транскрипта",   
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP 909, GR_Protein, 16, "GENE_PHENO", "Gene-phenotype association", 
"Indicates if gene is associated with a phenotype, disease or trait", 
"Указывает, связан ли ген с фенотипом, заболеванием или признаком",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP 910, GR_Protein, 17, "SIFT", "SIFT prediction and/or score", 
"SIFT prediction and/or score", 
"Предсказание и/или оценка SIFT",   
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP 911, GR_Protein, 18, "PolyPhen", "PolyPhen prediction and/or score", 
"PolyPhen prediction and/or score", 
"Предсказание и/или оценка PolyPhen",   
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP "1001", GR_Protein_add, 1, "HGVSc", "HGVS кодовая последовательность", 
"HGVS coding sequence name", 
"Имя кодовой последовательности HGVS",   
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP "1002", GR_Protein_add, 1, "HGVSp", "HGVS белковая последовательность", 
"HGVS protein sequence name", 
"Имя белковой последовательности HGVS",   
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP "1003", GR_Protein_add, 1, "HGVS_OFFSET", "Сдвиг HGVS", 
"Indicates by how many bases the HGVS notations for this variant have been shifted", 
"Указывает, на сколько оснований сдвинуты обозначения HGVS для этого варианта",   
"SNV", "query", INT />   

<@ATTRIBUTE_VEP "1004", GR_Protein_add, 1, "CLIN_SIG", "Клиническая значимость", 
"ClinVar clinical significance of the dbSNP variant", 
"Клиническая значимость варианта dbSNP из ClinVar",   
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP "1005", GR_Protein_add, 1, "SOMATIC", "Соматический статус", 
"Somatic status of existing variant", 
"Соматический статус существующего варианта",   
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP "1101", GR_Other, 1, "EXON", "Номер экзона", 
"Exon number(s) / total", 
"Номер(а) экзона / всего", 
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP "1102", GR_Other, 2, "INTRON", "Номер интрона", 
"Intron number(s) / total", 
"Номер(а) интрона / всего", 
"transcript", "query", STRING />   

<@ATTRIBUTE_VEP "1103", GR_Other, 3, "DOMAINS", "Перекрывающиеся белковые домены", 
"The source and identifier of any overlapping protein domains", 
"Источник и идентификатор любых перекрывающихся белковых доменов", 
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP "1104", GR_Other, 4, "miRNA", "miRNA структуры", 
"SO terms of overlapped miRNA secondary structure feature(s)", 
"SO термины для перекрывающихся особенностей вторичной структуры miRNA", 
"SNV", "query", STRING />   

<@ATTRIBUTE_VEP "1105", GR_Other, 5, "HGVSc", "HGVS кодирующая последовательность", 
"HGVS coding sequence name", 
"Имя HGVS кодирующей последовательности", 
"transcript", "query", STRING />  