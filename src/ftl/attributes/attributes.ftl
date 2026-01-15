DELETE FROM attributes;
DELETE FROM attribute_resources;

<#assign VEP = 57>
<#macro ATTRIBUTE id, group, order, title, title_ru, description, description_ru, 
   level, storage, type, comment="null", dictionary="null", displayIfEmpty="'no'", urlMask="null" >  
    INSERT INTO attributes(id, entity, groupID, displayOrder, title, title_ru, description, description_ru, 
       level, storage, type, comment, dictionary, displayIfEmpty, urlMask)
    VALUES( ${id}, 'snv_',  ${group}, ${order}, ${title?str}, ${title_ru?str}, ${description?str}, ${description_ru?str}, 
    ${level?str}, ${storage?str}, ${type}, ${comment}, ${dictionary}, ${displayIfEmpty}, ${urlMask} );
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
<#assign GR_ACMG_CRITERIA      = 13>

<#assign INT    = 1 /> 
<#assign DOUBLE = 2 /> 
<#assign STRING = 3 /> 


<@ATTRIBUTE '101', GR_COMMON, 1, "dbSNP", "dbSNP",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '1016', '101', 6/>

<@ATTRIBUTE '102', GR_COMMON, 1, "VarSome", "VarSome",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '10259', '102', 59/>

<@ATTRIBUTE '103', GR_COMMON, 1, "ClinVar", "ClinVar",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '10311', '103', 11/>
<@RESOURCE '10371', '103', 71/>

<@ATTRIBUTE '104', GR_COMMON, 1, "COSMIC", "COSMIC",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '10460', '104', 60/>

<@ATTRIBUTE '105', GR_COMMON, 1, "Franklin", "Franklin",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '10561', '105', 61/>

<@ATTRIBUTE '106', GR_COMMON, 1, "UCSC", "UCSC",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '10662', '106', 62/>

<@ATTRIBUTE '107', GR_COMMON, 1, "gnomAD 3", "gnomAD 3",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '10764', '107', 64/>

<@ATTRIBUTE '108', GR_COMMON, 1, "RUSeq", "RUSeq",
"Links to variant information pages in dbSNP (rsId), VarSome, ClinVar, COSMIC (if the database was uploaded as a custom annotation), Franklin, UCSC, gnomAD 3, RUSeq (if the database was uploaded as a custom annotation).",
"Ссылки на страницы с информацией о варианте в базах данных dbSNP (rsId), VarSome, ClinVar, COSMIC (если база была загружена в виде пользовательской аннотации), Franklin, UCSC, gnomAD 3, RUSeq (если база была загружена в виде пользовательской аннотации).",
"SNV", "query", INT />
<@RESOURCE '10863', '108', 63/>

<@ATTRIBUTE '109', GR_COMMON, 1, "Name", "Имя",
"Sample name for which the analysis was performed.",
"Имя образца, для которого проводился анализ.",
"SNV", "query", INT />

<@ATTRIBUTE '110', GR_COMMON, 1, "Genotype", "Генотип",
"Genotype, a combination of alleles for a given sample (0 - reference allele, 1 - first alternative allele, 2 - second alternative allele, etc.), separated by “/” (for an unphased genotype) or “|” (for phased genotype). The number of alleles indicates the ploidy of the sample.",
"Генотип, комбинация аллелей для данного образца (0 - референсный аллель, 1 - первый альтернативный аллель, 2 - второй альтернативный аллель и т.д.), разделенных ”/” (для нефазированного генотипа) или “|” (для фазированного генотипа). Количество аллелей говорит о плоидности образца.",
"SNV", "query", INT />
<@RESOURCE '11058', '110', 58/>

<@ATTRIBUTE '111', GR_COMMON, 1, "Read depth", "Глубина секвенирования",
"Sequencing depth, the total number of reads overlapping the variant position for the given sample.",
"Глубина секвенирования, общее количество прочтений последовательности, перекрывающих позицию варианта, для данного образца.",
"SNV", "query", INT />
<@RESOURCE '11158', '111', 58/>

<@ATTRIBUTE '112', GR_COMMON, 1, "Ref count", "Количество референсных аллелей",
"Number of times the reference allele was read for the given sample.",
"Количество раз, когда в последовательности считывался референсный аллель для данного образца.",
"SNV", "query", INT />
<@RESOURCE '11258', '112', 58/>

<@ATTRIBUTE '113', GR_COMMON, 1, "Alt count", "Количество альтернативных аллелей",
"Number of times the alternate allele was read for the given sample.",
"Количество раз, когда в последовательности считывался альтернативный аллель для данного образца.",
"SNV", "query", INT />
<@RESOURCE '11358', '113', 58/>

<@ATTRIBUTE '114', GR_COMMON, 1, "AF", "Частота альтернативного аллеля",
"Alternate allele frequency for the given sample.",
"Частота альтернативного аллеля для данного образца.",
"SNV", "query", INT />

<@ATTRIBUTE '115', GR_COMMON, 1, "Occurrence in samples", "Встречаемость в образцах",
"Occurrence in samples.",
"Встречаемость в образцах.",
"SNV", "query", INT />
<@RESOURCE '11558', '115', 58/>

<@ATTRIBUTE '116', GR_COMMON, 1, "Occurrence at launch", "Встречаемость в запуске",
"Occurrence in the launch.",
"Встречаемость в запуске.",
"SNV", "query", INT />

<@ATTRIBUTE '117', GR_COMMON, 1, "Variant interpretation/comment", "Интерпретация варианта/комментарий",
"Comment with important information about the variant, added by the user.",
"Комментарий с важной информацией о варианте, добавленный пользователем.",
"SNV", "query", INT />

<@ATTRIBUTE '118', GR_COMMON, 1, "Normalized version", "Нормализованная версия",
"Normalized variant. For some variants, the starting position, reference allele, or alternate allele presented in the uploaded VCF file of the sample may not match the left-normalized ones. In such cases, the variant will be displayed as in the original VCF file, and normalized data will be presented in the detail panel.",
"Нормализованный вариант. У некоторых вариантов стартовая позиция, референсный аллель или альтернативный аллель, приведенные в загруженном VCF файле образца, могут не совпадать с левонормализованными. В таком случае в таблице SNV Viewer вариант будет записан как в исходном VCF файле.",
"SNV", "query", INT />

<@ATTRIBUTE '201', GR_GENE, 2, "Full gene name", "Полное название гена",
"The full name of the gene. Hovering over the gene name will show which databases the full name was taken from.",
"Полное название гена. При наведении курсора на название гена можно увидеть, из каких баз данных было взято полное название.",
"SNV", "query", INT />

<@ATTRIBUTE '202', GR_GENE, 2, "Ensembl", "Ensembl",
"Links to gene information pages in various databases.",
"Ссылки на страницы с информацией о гене в различных базах данных.",
"SNV", "query", INT />
<@RESOURCE '20265', '202', 65/>

<@ATTRIBUTE '203', GR_GENE, 2, "UniProt", "UniProt",
"Links to gene information pages in various databases.",
"Ссылки на страницы с информацией о гене в различных базах данных.",
"SNV", "query", INT />
<@RESOURCE '20366', '203', 66/>

<@ATTRIBUTE '204', GR_GENE, 2, "GTEx", "GTEx",
"Links to gene information pages in various databases.",
"Ссылки на страницы с информацией о гене в различных базах данных.",
"SNV", "query", INT />
<@RESOURCE '20426', '204', 26/>

<@ATTRIBUTE '205', GR_GENE, 2, "OMIM", "OMIM",
"Links to gene information pages in various databases.",
"Ссылки на страницы с информацией о гене в различных базах данных.",
"SNV", "query", INT />
<@RESOURCE '2059', '205', 9/>
<@RESOURCE '20570', '205', 70/>

<@ATTRIBUTE '206', GR_GENE, 2, "NCBI", "NCBI",
"Links to gene information pages in various databases.",
"Ссылки на страницы с информацией о гене в различных базах данных.",
"SNV", "query", INT />
<@RESOURCE '20667', '206', 67/>

<@ATTRIBUTE '207', GR_GENE, 2, "Orphanet", "Orphanet",
"Links to gene information pages in various databases.",
"Ссылки на страницы с информацией о гене в различных базах данных.",
"SNV", "query", INT />
<@RESOURCE '20768', '207', 68/>

<@ATTRIBUTE '208', GR_GENE, 2, "Finding all variants in a gene", "Поиск всех вариантов в гене",
"Search for all variants identified in the sample located in the same gene. To search, click on the link 'All variants in the gene' — a page in SNV Viewer will open in a separate tab with filtering by this gene.",
"Поиск всех вариантов, выявленных в данном образце и расположенных в этом же гене. Для поиска нажмите на ссылку 'Все варианты в гене' - в соседней вкладке откроется страница SNV Viewer с фильтрацией по данному гену.",
"transcript", "query", INT />

<@ATTRIBUTE '209', GR_GENE, 2, "Transcript", "Транскрипт",
"The transcript identifier from the Ensembl database (ENSTxxxxxxxxxxx). For the first main transcript, the RefSeq transcript (NM_xxxxxx.x) is also provided. By clicking on the identifiers, you can open transcript pages in these databases. The gene's main transcript, according to MANE Select, MANE plus clinical, or Ensembl canonical, is marked. The transcript selected as the main for this variant is highlighted.",
"Идентификатор транскрипта из базы Ensembl (ENSTxxxxxxxxxxx). Для первого, основного транскрипта также приведен транскрипт из RefSeq (NM_xxxxxx.x). Нажав на идентификаторы, можно открыть страницы транскриптов в этих базах. Основной транскрипт гена, согласно MANE Select, MANE plus clinical или Ensembl canonical, отмечен. Транскрипт гена, выбранный основным для этого варианта, выделен рамкой.",
"transcript", "query", INT />

<@ATTRIBUTE '210', GR_GENE, 2, "IMPACT", "IMPACT",
"The predicted impact of the variant on the protein (see the variant impact values in the annotation table). The HIGH effect is colored red, MODERATE yellow, and LOW and MODIFIER are gray.",
"Предсказанное значение эффекта варианта на белок (см. таблицу со значениями эффектов варианта в аннотации). Значение эффекта HIGH раскрашено красным, MODERATE - жёлтым, а LOW и MODIFIER - серым.",
"transcript", "query", INT />

<@ATTRIBUTE '211', GR_GENE, 2, "HGVSp", "Аминокислотная замена по HGVS",
"Amino acid substitution in HGVS nomenclature: prefix “p.” (protein) + reference amino acid + amino acid position in the protein + new amino acid.",
"Аминокислотная замена по номенклатуре HGVS: префикс “p.” (protein) + референсная аминокислота + позиция аминокислоты в белке + новая аминокислота.",
"transcript", "query", INT />
<@RESOURCE '21169', '211', 69/>

<@ATTRIBUTE '212', GR_GENE, 2, "HGVSc", "Нуклеотидная замена по HGVS",
"Nucleotide substitution in HGVS nomenclature: prefix “c.” (coding; for substitutions in the coding sequence) or “n.” (non-coding) + genomic position of the nucleotide + reference allele > alternate allele.",
"Нуклеотидная замена по номенклатуре HGVS: префикс “c.” (coding; для замены в кодирующей последовательности ДНК) или “n.” (non-coding; для замены в некодирующей последовательности ДНК) + геномная позиция замещенного нуклеотида + референсный аллель > альтернативный аллель.",
"transcript", "query", INT />
<@RESOURCE '21269', '212', 69/>

<@ATTRIBUTE '213', GR_GENE, 2, "Phenotype", "Фенотип",
"Full name of the phenotype associated with the gene. If the phenotype name has a question mark (e.g., '?Bleeding disorder, platelet-type, 22'), it indicates the relationship between the phenotype and the gene is preliminary. If the phenotype name is in square brackets (e.g., '[Urate oxidase deficiency]'), it mainly indicates genetic variations that result in abnormal laboratory test values. If the phenotype name is in curly brackets (e.g., '{Prostate cancer/brain cancer susceptibility, somatic}'), it indicates mutations that contribute to predisposition to multifactorial diseases or susceptibility to infections.",
"Полное название фенотипа, связанного с геном. Если перед названием фенотипа стоит знак вопроса (например, '?Bleeding disorder, platelet-type, 22'), это означает, что взаимосвязь между фенотипом и геном является предварительной. Если название фенотипа заключено в квадратные скобки (например, '[Urate oxidase deficiency]'), это указывает в основном на генетические вариации, которые приводят к явно аномальным значениям лабораторных тестов. Если название фенотипа заключено в фигурные скобки (например, '{Prostate cancer/brain cancer susceptibility, somatic}'), это обозначает мутации, которые способствуют предрасположенности к многофакторным заболеваниям или восприимчивости к инфекциям.",
"SNV", "query", INT />

<@ATTRIBUTE '214', GR_GENE, 2, "Phenotype MIM number", "Номер MIM фенотипа",
"Identifier of the phenotype in the OMIM database. Clicking the link will take you to the phenotype's page in OMIM.",
"Идентификатор фенотипа в базе OMIM. Перейдя по ссылке, вы попадете на страницу фенотипа в OMIM.",
"SNV", "query", INT />

<@ATTRIBUTE '215', GR_GENE, 2, "Inheritance", "Наследование",
"Type of phenotype inheritance.",
"Тип наследования фенотипа.",
"SNV", "query", INT />

<@ATTRIBUTE '216', GR_GENE, 2, "Phenotype mapping key", "Ключ карты фенотипа",
"Key to the OMIM phenotype map (it shows the relationships between the phenotype and associated phenotypes, groups of related phenotypes, and genes).",
"Ключ карты фенотипа в OMIM (карта отображает связи данного фенотипа с ассоциированными фенотипами, группами родственных фенотипов и генами).",
"SNV", "query", INT />

<@ATTRIBUTE '301', GR_ClinVar, 3, "Phenotype", "Фенотип",
"Name of the phenotype (with links to various databases).",
"Название фенотипа (со ссылками на различные базы данных).",
"SNV", "query", INT />

<@ATTRIBUTE '302', GR_ClinVar, 3, "Clinical Significance", "Клиническая значимость",
"Clinical significance of the phenotype.",
"Клиническая значимость фенотипа.",
"SNV", "query", INT />

<@ATTRIBUTE '303', GR_ClinVar, 3, "Review status", "Статус рецензирования",
"Review status of the data source where the clinical significance was reported.",
"Оценка источника данных, в котором была заявлена клиническая значимость фенотипа.",
"SNV", "query", INT />

<@ATTRIBUTE '304', GR_ClinVar, 3, "Variant Haplotype ID", "Идентификатор гаплотипа варианта",
"Identifier of the haplotype containing the variant.",
"Идентификатор гаплотипа, в который входит вариант.",
"SNV", "query", INT />

<@ATTRIBUTE '305', GR_ClinVar, 3, "nan", "nan",
"Table of phenotypes observed in people with this haplotype and reported in ClinVar.",
"Таблица с фенотипами, наблюдаемыми у людей с этим гаплотипом и сообщёнными в ClinVar.",
"SNV", "query", INT />

<@ATTRIBUTE '306', GR_ClinVar, 3, "Clinical significance", "Клиническая значимость",
"Summary clinical significance of all phenotypes observed in people with this variant.",
"Суммарная клиническая значимость всех фенотипов, наблюдаемых у людей с этим вариантом.",
"SNV", "query", INT />

<@ATTRIBUTE '307', GR_ClinVar, 3, "ClinVar ID", "Идентификатор ClinVar",
"Identifier of the variant in ClinVar.",
"Идентификатор варианта в ClinVar.",
"SNV", "query", INT />

<@ATTRIBUTE '308', GR_ClinVar, 3, "Review status", "Статус рецензирования",
"Summary review status of all sources where the clinical significance of associated phenotypes was reported.",
"Суммарная оценка всех источников, заявивших клиническую значимость фенотипов, ассоциированных с вариантом.",
"SNV", "query", INT />

<@ATTRIBUTE '309', GR_ClinVar, 3, "Allele ID", "Идентификатор аллеля",
"Identifier of the variant allele in ClinVar.",
"Идентификатор аллеля варианта в ClinVar.",
"SNV", "query", INT />

<@ATTRIBUTE '310', GR_ClinVar, 3, "Allele origin", "Происхождение аллеля",
"Summary origin of the variant allele from various sources provided by ClinVar.",
"Суммарное происхождение аллеля варианта из различных источников, предоставленных ClinVar.",
"SNV", "query", INT />

<@ATTRIBUTE '311', GR_ClinVar, 3, "Cross references", "Кросс-ссылки",
"Links to the variant in various databases.",
"Ссылки на вариант в различных базах данных.",
"SNV", "query", INT />

<@ATTRIBUTE '401', GR_gnomAD_3, 4, "Allele frequency", "Частота аллеля",
"Summarized frequency of the alternative allele in high-quality genotypes.",
"Суммарная частота альтернативного аллеля в высококачественных генотипах.",
"SNV", "query", INT />

<@ATTRIBUTE '402', GR_gnomAD_3, 4, "Coverage", "Покрытие",
"Average depth of coverage by bases (ranges: <10, 10-100, ⩾100).",
"Средняя глубина покрытия по основаниям (диапазоны <10, 10-100, ⩾100).",
"SNV", "query", INT />

<@ATTRIBUTE '403', GR_gnomAD_3, 4, "Number of Homozygotes", "Число гомозигот",
"The number of individuals homozygous for the alternative allele in the population.",
"Количество людей, гомозиготных по альтернативному аллелю в популяции.",
"SNV", "query", INT />

<@ATTRIBUTE '404', GR_gnomAD_3, 4, "XY samples AF", "Частота аллеля в XY популяции",
"Summarized frequency of the alternative allele in the XY subpopulation.",
"Суммарная частота альтернативного аллеля в подпопуляции XY.",
"SNV", "query", INT />

<@ATTRIBUTE '405', GR_gnomAD_3, 4, "XX samples AF", "Частота аллеля в XX популяции",
"Summarized frequency of the alternative allele in the XX subpopulation.",
"Суммарная частота альтернативного аллеля в подпопуляции XX.",
"SNV", "query", INT />

<@ATTRIBUTE '406', GR_gnomAD_3, 4, "gnomADe_AF", "gnomADe_AF",
"Frequency of existing variant in gnomAD exomes combined population",
"Частота варианта в объединённой популяции экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '40664', '406', 64/>

<@ATTRIBUTE '407', GR_gnomAD_3, 4, "gnomADe_AFR_AF", "gnomADe_AFR_AF",
"Frequency of existing variant in gnomAD exomes African/American population",
"Частота варианта в афроамериканской популяции экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '40764', '407', 64/>

<@ATTRIBUTE '408', GR_gnomAD_3, 4, "gnomADe_AMR_AF", "gnomADe_AMR_AF",
"Frequency of existing variant in gnomAD exomes American population",
"Частота варианта в американской популяции экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '40864', '408', 64/>

<@ATTRIBUTE '409', GR_gnomAD_3, 4, "gnomADe_ASJ_AF", "gnomADe_ASJ_AF",
"Frequency of existing variant in gnomAD exomes Ashkenazi Jewish population",
"Частота варианта в популяции ашкеназских евреев экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '40964', '409', 64/>

<@ATTRIBUTE '410', GR_gnomAD_3, 4, "gnomADe_EAS_AF", "gnomADe_EAS_AF",
"Frequency of existing variant in gnomAD exomes East Asian population",
"Частота варианта в восточноазиатской популяции экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41064', '410', 64/>

<@ATTRIBUTE '411', GR_gnomAD_3, 4, "gnomADe_FIN_AF", "gnomADe_FIN_AF",
"Frequency of existing variant in gnomAD exomes Finnish population",
"Частота варианта в финской популяции экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41164', '411', 64/>

<@ATTRIBUTE '412', GR_gnomAD_3, 4, "gnomADe_NFE_AF", "gnomADe_NFE_AF",
"Frequency of existing variant in gnomAD exomes Non-Finnish European population",
"Частота варианта в не-финской европейской популяции экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41264', '412', 64/>

<@ATTRIBUTE '413', GR_gnomAD_3, 4, "gnomADe_OTH_AF", "gnomADe_OTH_AF",
"Frequency of existing variant in gnomAD exomes other combined populations",
"Частота варианта в других объединённых популяциях экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41364', '413', 64/>

<@ATTRIBUTE '414', GR_gnomAD_3, 4, "gnomADe_SAS_AF", "gnomADe_SAS_AF",
"Frequency of existing variant in gnomAD exomes South Asian population",
"Частота варианта в южноазиатской популяции экзомов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41464', '414', 64/>

<@ATTRIBUTE '415', GR_gnomAD_3, 4, "gnomADg_AF", "gnomADg_AF",
"Frequency of existing variant in gnomAD genomes combined population",
"Частота варианта в объединённой популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41564', '415', 64/>

<@ATTRIBUTE '416', GR_gnomAD_3, 4, "gnomADg_AFR_AF", "gnomADg_AFR_AF",
"Frequency of existing variant in gnomAD genomes African/American population",
"Частота варианта в афроамериканской популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41664', '416', 64/>

<@ATTRIBUTE '417', GR_gnomAD_3, 4, "gnomADg_AMI_AF", "gnomADg_AMI_AF",
"Frequency of existing variant in gnomAD genomes Amish population",
"Частота варианта в популяции амишей геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41764', '417', 64/>

<@ATTRIBUTE '418', GR_gnomAD_3, 4, "gnomADg_AMR_AF", "gnomADg_AMR_AF",
"Frequency of existing variant in gnomAD genomes American population",
"Частота варианта в американской популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41864', '418', 64/>

<@ATTRIBUTE '419', GR_gnomAD_3, 4, "gnomADg_ASJ_AF", "gnomADg_ASJ_AF",
"Frequency of existing variant in gnomAD genomes Ashkenazi Jewish population",
"Частота варианта в популяции ашкеназских евреев геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '41964', '419', 64/>

<@ATTRIBUTE '420', GR_gnomAD_3, 4, "gnomADg_EAS_AF", "gnomADg_EAS_AF",
"Frequency of existing variant in gnomAD genomes East Asian population",
"Частота варианта в восточноазиатской популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '42064', '420', 64/>

<@ATTRIBUTE '421', GR_gnomAD_3, 4, "gnomADg_FIN_AF", "gnomADg_FIN_AF",
"Frequency of existing variant in gnomAD genomes Finnish population",
"Частота варианта в финской популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '42164', '421', 64/>

<@ATTRIBUTE '422', GR_gnomAD_3, 4, "gnomADg_MID_AF", "gnomADg_MID_AF",
"Frequency of existing variant in gnomAD genomes Mid-eastern population",
"Частота варианта в ближневосточной популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '42264', '422', 64/>

<@ATTRIBUTE '423', GR_gnomAD_3, 4, "gnomADg_NFE_AF", "gnomADg_NFE_AF",
"Frequency of existing variant in gnomAD genomes Non-Finnish European population",
"Частота варианта в не-финской европейской популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '42364', '423', 64/>

<@ATTRIBUTE '424', GR_gnomAD_3, 4, "gnomADg_OTH_AF", "gnomADg_OTH_AF",
"Frequency of existing variant in gnomAD genomes other combined populations",
"Частота варианта в других объединённых популяциях геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '42464', '424', 64/>

<@ATTRIBUTE '425', GR_gnomAD_3, 4, "gnomADg_SAS_AF", "gnomADg_SAS_AF",
"Frequency of existing variant in gnomAD genomes South Asian population",
"Частота варианта в южноазиатской популяции геномов gnomAD",
"SNV", "query", INT />
<@RESOURCE '42564', '425', 64/>

<@ATTRIBUTE '501', GR_gnomAD_4, 5, "Allele frequency (AF)", "Частота аллеля (AF)",
"Sum of alternative allele frequency in high-quality genotypes. For frequency, AC - number of alternative alleles in high-quality genotypes, and AN - total number of high-quality genotypes.",
"Суммарная частота альтернативного аллеля в генотипах высокого качества. Для частоты приведены AC - количество альтернативного аллеля варианта в генотипах высокого качества, и AN - общее количество генотипов высокого качества.",
"SNV", "query", INT />

<@ATTRIBUTE '502', GR_gnomAD_4, 5, "Number of Homozygotes", "Количество гомозиготных",
"Number of individuals homozygous for the alternative allele in the population. For the number of homozygotes, XY samples and XX samples are given - the number of individuals homozygous for the alternative allele in the XY and XX subpopulations, respectively.",
"Количество людей, гомозиготных по альтернативному аллелю, в популяции. Для количества гомозигот приведены XY samples и XX samples - количество людей, гомозиготных по альтернативному аллелю, в подпопуляции XY и подпопуляции XX, соответственно.",
"SNV", "query", INT />

<@ATTRIBUTE '503', GR_gnomAD_4, 5, "XY samples AF (XY AF)", "Частота аллеля в подгруппе XY (XY AF)",
"Sum of alternative allele frequency in XY subpopulation. For frequency, XY samples AC (XY AC) - number of alternative alleles in high-quality genotypes in the XY subpopulation, and XY samples AN (XY AN) - total number of high-quality genotypes in the XY subpopulation.",
"Суммарная частота альтернативного аллеля в подпопуляции XY. Для частоты приведены XY samples AC (XY AC) - количество альтернативного аллеля варианта в генотипах высокого качества в подпопуляции XY, и XY samples AN (XY AN) - общее количество генотипов высокого качества в подпопуляции XY.",
"SNV", "query", INT />

<@ATTRIBUTE '504', GR_gnomAD_4, 5, "XX samples AF (XX AF)", "Частота аллеля в подгруппе XX (XX AF)",
"Sum of alternative allele frequency in XX subpopulation. For frequency, XX samples AC (XX AC) - number of alternative alleles in high-quality genotypes in the XX subpopulation, and XX samples AN (XX AN) - total number of high-quality genotypes in the XX subpopulation.",
"Суммарная частота альтернативного аллеля в подпопуляции XX. Для частоты приведены XX samples AC (XX AC) - количество альтернативного аллеля варианта в генотипах высокого качества в подпопуляции XX, и XX samples AN (XX AN) - общее количество генотипов высокого качества в подпопуляции XX.",
"SNV", "query", INT />

<@ATTRIBUTE '601', GR_ExAC, 6, "Allele frequency", "Частота аллеля",
"Summary frequency of the alternative allele of the variant.",
"Суммарная частота альтернативного аллеля варианта.",
"SNV", "query", INT />

<@ATTRIBUTE '602', GR_ExAC, 6, "Adjusted AF", "Скорректированная частота аллеля",
"Frequency of the alternative allele in the population with genotype quality ≥20 and depth ≥10.",
"Частота альтернативного аллеля в популяции с качеством генотипа ≥20 и глубиной ≥10.",
"SNV", "query", INT />

<@ATTRIBUTE '603', GR_ExAC, 6, "African & African American", "Африканцы и афроамериканцы",
"Frequencies in the African and African American populations.",
"Частоты в популяциях африканцев и афроамериканцев.",
"SNV", "query", INT />

<@ATTRIBUTE '604', GR_ExAC, 6, "American", "Американцы",
"Frequencies in the American population.",
"Частоты в популяции американцев.",
"SNV", "query", INT />

<@ATTRIBUTE '605', GR_ExAC, 6, "East Asian", "Восточные азиаты",
"Frequencies in the East Asian population.",
"Частоты в популяции восточных азиатов.",
"SNV", "query", INT />

<@ATTRIBUTE '606', GR_ExAC, 6, "Finnish", "Финны",
"Frequencies in the Finnish population.",
"Частоты в популяции финнов.",
"SNV", "query", INT />

<@ATTRIBUTE '607', GR_ExAC, 6, "Non-Finnish European", "Нефинские европейцы",
"Frequencies in the Non-Finnish European population.",
"Частоты в популяции нефинских европейцев.",
"SNV", "query", INT />

<@ATTRIBUTE '608', GR_ExAC, 6, "South Asian", "Южные азиаты",
"Frequencies in the South Asian population.",
"Частоты в популяции южных азиатов.",
"SNV", "query", INT />

<@ATTRIBUTE '701', GR_Other_freq, 7, "1000 G Allele frequency", "Частота аллеля 1000 G",
"Summary allele frequency of the variant.",
"Суммарная частота альтернативного аллеля варианта.",
"SNV", "query", INT />

<@ATTRIBUTE '702', GR_Other_freq, 7, "1000 G East Asian", "Восточная Азия 1000 G",
"Frequencies in the East Asian population.",
"Частоты в популяции Восточной Азии.",
"SNV", "query", INT />

<@ATTRIBUTE '703', GR_Other_freq, 7, "1000 G European", "Европейская популяция 1000 G",
"Frequencies in the European population.",
"Частоты в популяции Европы.",
"SNV", "query", INT />

<@ATTRIBUTE '704', GR_Other_freq, 7, "1000 G African", "Африканская популяция 1000 G",
"Frequencies in the African population.",
"Частоты в популяции Африки.",
"SNV", "query", INT />

<@ATTRIBUTE '705', GR_Other_freq, 7, "1000 G American", "Американская популяция 1000 G",
"Frequencies in the American population.",
"Частоты в популяции Америки.",
"SNV", "query", INT />

<@ATTRIBUTE '706', GR_Other_freq, 7, "1000 G South Asian", "Южноазиатская популяция 1000 G",
"Frequencies in the South Asian population.",
"Частоты в популяции Южной Азии.",
"SNV", "query", INT />

<@ATTRIBUTE '707', GR_Other_freq, 7, "UK10K Allele frequency", "Частота аллеля UK10K",
"Summary allele frequency in the combined genotypes of the UK10K cohort (TwinsUK + ALSPAC).",
"Суммарная частота альтернативного аллеля варианта в комбинированных генотипах в когорте UK10K (TwinsUK + ALSPAC).",
"SNV", "query", INT />

<@ATTRIBUTE '708', GR_Other_freq, 7, "UK10K TWINSUK AF", "Частота аллеля в TWINSUK",
"Allele frequency in TwinsUK cohort, which includes adult twins living in the UK.",
"Частота альтернативного аллеля в TwinsUK — когорте, включающей взрослых близнецов, проживающих в Великобритании.",
"SNV", "query", INT />

<@ATTRIBUTE '709', GR_Other_freq, 7, "UK10K ALSPAC AF", "Частота аллеля в ALSPAC",
"Allele frequency in ALSPAC cohort, which includes fetal tissue samples.",
"Частота альтернативного аллеля в ALSPAC — когорте, включающей образцы тканей плодов.",
"SNV", "query", INT />

<@ATTRIBUTE '710', GR_Other_freq, 7, "UK10K Allele count", "Количество аллелей UK10K",
"Count of alternative alleles in high-quality genotypes.",
"Количество альтернативного аллеля в генотипах высокого качества.",
"SNV", "query", INT />

<@ATTRIBUTE '711', GR_Other_freq, 7, "ESP African American", "Афроамериканская популяция ESP",
"Frequencies in the population.",
"Частоты в популяции.",
"SNV", "query", INT />

<@ATTRIBUTE '712', GR_Other_freq, 7, "ESP European American", "Европейская популяция ESP",
"Frequencies in the population.",
"Частоты в популяции.",
"SNV", "query", INT />

<@ATTRIBUTE '713', GR_Other_freq, 7, "ESP Allele count", "Количество аллелей ESP",
"Count of alternative alleles in high-quality genotypes.",
"Количество альтернативного аллеля в генотипах высокого качества.",
"SNV", "query", INT />

<@ATTRIBUTE '801', GR_Conservation, 8, "Ancestral allele", "Предковый аллель",
"Ancestral allele obtained from multiple whole genome alignments using the Ortheus probabilistic method.",
"Предковый аллель, полученный из множественных выравнивании полного генома с использованием вероятностного метода Ortheus.",
"SNV", "query", INT />

<@ATTRIBUTE '802', GR_Conservation, 8, "Altai Neanderthal", "Неандерталец из Алтая",
"Genotype of the deeply sequenced genome of the Altai Neanderthal.",
"Генотип глубоко секвенированного генома неандертальца Алтай.",
"SNV", "query", INT />

<@ATTRIBUTE '803', GR_Conservation, 8, "Denisova", "Денисовец",
"Genotype of the deeply sequenced genome of the Denisovan.",
"Генотип глубоко секвенированного генома денисовского человека.",
"SNV", "query", INT />

<@ATTRIBUTE '804', GR_Conservation, 8, "Vindija Neanderthal", "Неандерталец из Виндий",
"Genotype of the deeply sequenced genome of the Vindija Neanderthal.",
"Генотип глубоко секвенированного генома неандертальца Виндий.",
"SNV", "query", INT />

<@ATTRIBUTE '805', GR_Conservation, 8, "Integrated FitCons rankscore", "Интегрированный показатель FitCons",
"Integrated measure obtained by combining information from three cell lines (HUVEC, H1 hESC, and GM12878).",
"Интегрированный показатель, полученный путем объединения информации из трех клеточных линий (HUVEC, H1 hESC и GM12878).",
"SNV", "query", INT />

<@ATTRIBUTE '806', GR_Conservation, 8, "GM12878 FitCons rankscore", "GM12878 FitCons rankscore",
"GM12878 FitCons rankscore, H1-hESC FitCons rankscore, and HUVEC fitCons rankscore - conservation scores in cell lines.",
"GM12878 FitCons rankscore, H1-hESC FitCons rankscore и HUVEC fitCons rankscore - оценки консервативности в клеточных линиях GM12878, H1 hESC и HUVEC.",
"SNV", "query", INT />

<@ATTRIBUTE '807', GR_Conservation, 8, "H1-hESC FitCons rankscore", "H1-hESC FitCons rankscore",
"GM12878 FitCons rankscore, H1-hESC FitCons rankscore, and HUVEC fitCons rankscore - conservation scores in cell lines.",
"GM12878 FitCons rankscore, H1-hESC FitCons rankscore и HUVEC fitCons rankscore - оценки консервативности в клеточных линиях GM12878, H1 hESC и HUVEC.",
"SNV", "query", INT />

<@ATTRIBUTE '808', GR_Conservation, 8, "HUVEC fitCons rankscore", "HUVEC fitCons rankscore",
"GM12878 FitCons rankscore, H1-hESC FitCons rankscore, and HUVEC fitCons rankscore - conservation scores in cell lines.",
"GM12878 FitCons rankscore, H1-hESC FitCons rankscore и HUVEC fitCons rankscore - оценки консервативности в клеточных линиях GM12878, H1 hESC и HUVEC.",
"SNV", "query", INT />

<@ATTRIBUTE '809', GR_Conservation, 8, "GERP++ RS rankscore", "GERP++ RS rankscore",
"GERP++ RS rankscore.",
"GERP++ RS rankscore.",
"SNV", "query", INT />
<@RESOURCE '80973', '809', 73/>

<@ATTRIBUTE '810', GR_Conservation, 8, "GERP neutral rate", "Нейтральный уровень GERP",
"GERP Neutral rate.",
"Нейтральный уровень.",
"SNV", "query", INT />
<@RESOURCE '81073', '810', 73/>

<@ATTRIBUTE '811', GR_Conservation, 8, "GERP score", "Оценка GERP",
"GERP Score.",
"Оценка.",
"SNV", "query", INT />
<@RESOURCE '81173', '811', 73/>

<@ATTRIBUTE '812', GR_Conservation, 8, "PhyloP vertebrate conservation rankscore", "Оценка консервации позвоночных (PhyloP)",
"PhyloP vertebrate conservation rankscore, PhyloP mammalian conservation rankscore, and PhyloP primate conservation rankscore - conservation scores obtained from vertebrate, mammalian, and primate genome alignments.",
"Оценка консервативности позвоночных, млекопитающих и приматов, полученная из выравнивания геномов позвоночных, млекопитающих и приматов.",
"SNV", "query", INT />
<@RESOURCE '81274', '812', 74/>

<@ATTRIBUTE '813', GR_Conservation, 8, "PhyloP mammalian conservation rankscore", "Оценка консервации млекопитающих (PhyloP)",
"PhyloP vertebrate conservation rankscore, PhyloP mammalian conservation rankscore, and PhyloP primate conservation rankscore - conservation scores obtained from vertebrate, mammalian, and primate genome alignments.",
"Оценка консервативности позвоночных, млекопитающих и приматов, полученная из выравнивания геномов позвоночных, млекопитающих и приматов.",
"SNV", "query", INT />
<@RESOURCE '81374', '813', 74/>

<@ATTRIBUTE '814', GR_Conservation, 8, "PhyloP primate conservation rankscore", "Оценка консервации приматов (PhyloP)",
"PhyloP vertebrate conservation rankscore, PhyloP mammalian conservation rankscore, and PhyloP primate conservation rankscore - conservation scores obtained from vertebrate, mammalian, and primate genome alignments.",
"Оценка консервативности позвоночных, млекопитающих и приматов, полученная из выравнивания геномов позвоночных, млекопитающих и приматов.",
"SNV", "query", INT />
<@RESOURCE '81474', '814', 74/>

<@ATTRIBUTE '815', GR_Conservation, 8, "PhastCons vertebrate conservation rankscore", "Оценка консервации позвоночных (PhastCons)",
"PhastCons vertebrate conservation rankscore, PhastCons mammalian conservation rankscore, and PhastCons primate conservation rankscore - conservation scores obtained from vertebrate, mammalian, and primate genome alignments using phastCons.",
"Оценка консервативности позвоночных, млекопитающих и приматов, полученная из выравнивания геномов позвоночных, млекопитающих и приматов с использованием программы phastCons.",
"SNV", "query", INT />
<@RESOURCE '81575', '815', 75/>

<@ATTRIBUTE '816', GR_Conservation, 8, "PhastCons mammalian conservation rankscore", "Оценка консервации млекопитающих (PhastCons)",
"PhastCons vertebrate conservation rankscore, PhastCons mammalian conservation rankscore, and PhastCons primate conservation rankscore - conservation scores obtained from vertebrate, mammalian, and primate genome alignments using phastCons.",
"Оценка консервативности позвоночных, млекопитающих и приматов, полученная из выравнивания геномов позвоночных, млекопитающих и приматов с использованием программы phastCons.",
"SNV", "query", INT />
<@RESOURCE '81675', '816', 75/>

<@ATTRIBUTE '817', GR_Conservation, 8, "PhastCons primate conservation rankscore", "PhastCons primate conservation rankscore",
"PhastCons vertebrate conservation rankscore, PhastCons mammalian conservation rankscore, and PhastCons primate conservation rankscore are conservation scores obtained from the alignment of vertebrate, mammal, and primate genomes. PhastCons is a program for detecting evolutionarily conserved elements in multiple alignments, taking into account the phylogenetic tree, based on a phylogenetic hidden Markov model.",
"PhastCons vertebrate conservation rankscore, PhastCons mammalian conservation rankscore и PhastCons primate conservation rankscore - оценки консервативности, полученные из выравнивания геномов позвоночных, млекопитающих и приматов. PhastCons - это программа для выявления эволюционно консервативных элементов в множественном выравнивании с учетом филогенетического дерева, основанная на филогенетической скрытой марковской модели.",
"SNV", "query", INT />
<@RESOURCE '81775', '817', 75/>

<@ATTRIBUTE '818', GR_Conservation, 8, "SiPhy rankscore", "SiPhy rankscore",
"SiPhy rankscore and score are assessments of the conservativeness of the genomic position of the variant, obtained using the SiPhy program, employing a simple generalization of substitution pattern evaluation.",
"SiPhy rankscore и score - оценки консервативности геномной позиции варианта, полученные программой SiPhy, используя простое обобщение оценки паттерна замены.",
"SNV", "query", INT />
<@RESOURCE '81876', '818', 76/>

<@ATTRIBUTE '819', GR_Conservation, 8, "BStatistic rankscore", "BStatistic rankscore",
"BStatistic rankscore is an assessment of the conservativeness of the genomic position of the variant, derived from analyzing the genomic distribution of human polymorphisms and sequence differences among five primate species concerning the arrangement of conserved sequence patterns.",
"BStatistic rankscore - оценка консервативности геномной позиции варианта, полученная из анализа геномного распределения полиморфизмов человека и различий в последовательностях среди пяти видов приматов относительно расположения паттернов консервативных последовательностей.",
"SNV", "query", INT />
<@RESOURCE '81977', '819', 77/>

<@ATTRIBUTE '901', GR_Protein, 9, "SIFT", "SIFT",
"SIFT (Sorting Intolerant From Tolerant) predicts whether an amino acid substitution affects protein function based on sequence homology and physical properties of amino acids. The prediction output includes rankscore and score, which range from 0 to 1. An amino acid substitution is considered damaging if score ≤ 0.05, and tolerated if score > 0.05.",
"SIFT (Sorting Intolerant From Tolerant) предсказывает, влияет ли аминокислотная замена на функцию белка, основываясь на гомологии последовательностей и физических свойствах аминокислот. Результатом предсказания являются rankscore и score, значения которых варьируют от 0 до 1. Аминокислотная замена считается повреждающей, если score ≤ 0,05, и толерантной, если score > 0,05.",
"SNV", "query", INT />

<@ATTRIBUTE '902', GR_Protein, 9, "PolyPhen", "PolyPhen",
"PolyPhen (Polymorphism Phenotyping) predicts the possible impact of an amino acid substitution on protein structure and function, using eight properties of the protein sequence and three properties of protein structure.",
"PolyPhen (Polymorphism Phenotyping) предсказывает возможное влияние аминокислотной замены на структуру и функции белка, используя восемь свойств белковой последовательности и три свойства структуры белка.",
"SNV", "query", INT />

<@ATTRIBUTE '903', GR_Protein, 9, "SIFT4G", "SIFT4G",
"SIFT4G (SIFT Databases for Genomes) is a faster version of SIFT. The prediction output includes score, rankscore, and prediction: Tolerated (тolerантный) is colored green; Damaging (повреждающий) is red.",
"SIFT4G (SIFT Databases for Genomes) - более быстрая версия SIFT. Результатом предсказания являются score, rankscore, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />

<@ATTRIBUTE '904', GR_Protein, 9, "LRT", "LRT",
"LRT (Likelihood Ratio Test) identifies damaging mutations that disrupt highly conserved amino acids in protein-coding sequences based on an evolutionary model of DNA sequence. The prediction output includes score, rankscore, omega, prediction: Neutral (нейтральный) is colored green; Deleterious (повреждающий) is red.",
"LRT (Likelihood Ratio Test) идентифицирует повреждающие мутации, которые нарушают высококонсервативные аминокислоты в белок-кодирующих последовательностях, основываясь на эволюционной модели последовательности ДНК. Результатом предсказания являются score, rankscore, omega, prediction: Neutral (нейтральный) раскрашен зелёным; Deleterious (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '90478', '904', 78/>

<@ATTRIBUTE '905', GR_Protein, 9, "FAtHMM", "FAtHMM",
"FAtHMM (Functional Analysis through Hidden Markov Models) predicts functional consequences for both protein-coding variants and non-coding variants based on sequence homology. The prediction output includes score, rankscore, prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is red.",
"FAtHMM (Functional Analysis through Hidden Markov Models) прогнозирует функциональные последствия как белок-кодирующих вариантов, так и некодирующих вариантов, основываясь на гомологии последовательностей. Результатом предсказания являются score, rankscore, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />

<@ATTRIBUTE '906', GR_Protein, 9, "SpliceAI Delta score", "SpliceAI Delta score",
"Delta score (DS) is the probability that the variant affects splicing at any position within a specified interval around it (+/- 50 bp by default). Delta scores range from 0 to 1.",
"Delta score (DS) - вероятность того, что вариант влияет на сплайсинг в любой позиции в пределах заданного интервала вокруг него (+/- 50 п.н. по умолчанию). Оценки дельты варьируются от 0 до 1.",
"SNV", "query", INT />
<@RESOURCE '90656', '906', 56/>

<@ATTRIBUTE '907', GR_Protein, 9, "SpliceAI Delta position", "SpliceAI Delta position",
"Delta position is the position (in bp) relative to the variant where splicing changes (positive values — below the variant position, negative values — above). Delta position corresponds to the position value obtained for the indicator (Acceptor gain, Acceptor loss, Donor gain, or Donor loss) with the maximum DS among the others.",
"Delta position - позиция (в п.н.) относительно варианта, в которой изменяется сплайсинг (положительные значения — ниже позиции варианта, отрицательные значения — выше). Delta position соответствует значению позиции, полученной для показателя (Acceptor gain, Acceptor loss, Donor gain или Donor loss) с максимальным значением DS среди остальных.",
"SNV", "query", INT />
<@RESOURCE '90756', '907', 56/>

<@ATTRIBUTE '908', GR_Protein, 9, "SpliceAI Lookup link", "SpliceAI Lookup link",
"A link to the SpliceAI Lookup website, which provides more detailed information about the scores for the variant.",
"Ссылка на сайт SpliceAI Lookup, где приведена более расширенная информация об оценках для варианта.",
"SNV", "query", INT />
<@RESOURCE '90856', '908', 56/>

<@ATTRIBUTE '909', GR_Protein, 9, "CADD Raw", "CADD Raw",
"Raw - unprocessed score, relative value. It is interpreted as the degree to which the annotation profile for the given variant suggests that the variant is likely to be 'observed' (negative values) compared to 'modeled' (positive values). Higher score values indicate a higher likelihood of the variant having deleterious effects.",
"Raw - необработанная оценка, относительное значение. Интерпретируется как степень, в которой профиль аннотации для данного варианта предполагает, что вариант, вероятно, будет «наблюдаемым» (отрицательные значения) по сравнению с «моделированным» (положительные значения). Более высокие значения оценки указывают на то, что вариант с большей вероятностью будет иметь вредоносные последствия.",
"SNV", "query", INT />
<@RESOURCE '90980', '909', 80/>

<@ATTRIBUTE '910', GR_Protein, 9, "CADD Phred", "CADD Phred",
"Phred-scaled normalized score. Phred values < 10 are colored green, 10 ≤ Phred < 20 are orange, Phred ≥ 20 are red.",
"Phred-масштабированная нормализованная оценка. Значения Phred < 10 раскрашены зелёным, 10 ≤ Phred < 20 - оранжевым, Phred ≥ 20 - красным.",
"SNV", "query", INT />
<@RESOURCE '91080', '910', 80/>

<@ATTRIBUTE '911', GR_Protein, 9, "MaxEntScan Alt score", "MaxEntScan Alt score",
"MaxEntScan predicts the suitability of the variant site for splicing based on a maximum entropy model. The output includes scores for the alternative and reference nucleotides of the variant, predicting the loss of a natural splice site: Alt score and Ref score, and the difference between these scores: Diff. Values Alt score < -19 are colored red, -19 ≤ Alt score < -3 are orange, Alt score ≥ -3 are green.",
"MaxEntScan предсказывает пригодность сайта варианта для сплайсинга на основе модели максимальной энтропии. Результатом являются оценки для альтернативного и референсного нуклеотидов варианта, предсказывающие потерю естественного сайта сплайсинга: Alt score и Ref score, и разница между этими оценками: Diff. Значения Alt score < -19 раскрашены красным, -19 ≤ Alt score < -3 - оранжевым, Alt score ≥ -3 - зелёным.",
"SNV", "query", INT />
<@RESOURCE '91151', '911', 51/>

<@ATTRIBUTE '912', GR_Protein, 9, "MaxEntScan Ref score", "MaxEntScan Ref score",
"MaxEntScan predicts the suitability of the variant site for splicing based on a maximum entropy model. The output includes scores for the alternative and reference nucleotides of the variant, predicting the loss of a natural splice site: Alt score and Ref score, and the difference between these scores: Diff. Values Alt score < -19 are colored red, -19 ≤ Alt score < -3 are orange, Alt score ≥ -3 are green.",
"MaxEntScan предсказывает пригодность сайта варианта для сплайсинга на основе модели максимальной энтропии. Результатом являются оценки для альтернативного и референсного нуклеотидов варианта, предсказывающие потерю естественного сайта сплайсинга: Alt score и Ref score, и разница между этими оценками: Diff. Значения Alt score < -19 раскрашены красным, -19 ≤ Alt score < -3 - оранжевым, Alt score ≥ -3 - зелёным.",
"SNV", "query", INT />
<@RESOURCE '91251', '912', 51/>

<@ATTRIBUTE '913', GR_Protein, 9, "MaxEntScan Diff", "MaxEntScan Разница",
"MaxEntScan predicts the suitability of the variant site for splicing based on a maximum entropy model. The results include scores for the alternative and reference nucleotides of the variant, predicting the loss of the natural splice site: Alt score and Ref score, along with the difference between these scores: Diff. Values of Alt score < -19 are colored red, -19 ≤ Alt score < -3 are orange, and Alt score ≥ -3 are green.",
"MaxEntScan предсказывает пригодность сайта варианта для сплайсинга на основе модели максимальной энтропии. Результатом являются оценки для альтернативного и референсного нуклеотидов варианта, предсказывающие потерю естественного сайта сплайсинга: Alt score и Ref score, и разница между этими оценками: Diff. Значения Alt score < -19 раскрашены красным, -19 ≤ Alt score < -3 - оранжевым, Alt score ≥ -3 - зелёным.",
"SNV", "query", INT />
<@RESOURCE '91351', '913', 51/>

<@ATTRIBUTE '914', GR_Protein, 9, "MutationAssessor Prediction", "Предсказание MutationAssessor",
"Predicts the functional impact of the substitution: functional: High is colored red, Medium is orange, and non-functional: Low and Neutral are colored green.",
"Предсказание функциональности влияния замены: функциональное: High (высокое) раскрашено красным, Medium (среднее) - оранжевым, или нефункциональное: Low (низкое) и Neutral (нейтральное) раскрашены зелёным.",
"SNV", "query", INT />
<@RESOURCE '91451', '914', 51/>

<@ATTRIBUTE '915', GR_Protein, 9, "MutationAssessor Score", "Оценка MutationAssessor",
"Numerical prediction value. Scores ≥ 3.5 correspond to high impact, 3.5 < Score ≤ 1.935 to medium impact, 1.935 < Score ≤ 0.8 to low impact, and 0.8 < Score to neutral impact.",
"Численное значение предсказания. Значения Score ≥ 3,5 соответствуют высокому влиянию, 3,5 < Score ≤ 1,935 - среднему, 1,935 < Score ≤ 0,8 - низкому, 0,8 < Score - нейтральному.",
"SNV", "query", INT />
<@RESOURCE '91551', '915', 51/>

<@ATTRIBUTE '1000', GR_Protein_add, 10, "BayesDel addAF prediction", "Предсказание BayesDel addAF",
"Predicts the pathogenicity of variants considering MaxAF (the maximum allele frequency among populations): Tolerated (green); Damaging (red). The prediction values correspond to the addAF score and addAF rankscore, based on which the prediction was made. The threshold between scores corresponding to damaging and tolerated predictions is 0.0692655.",
"Предсказание вредоносности вариантов с учётом MaxAF (максимальной частоты аллеля среди популяций): Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным. Значению предсказания соответствуют оценки addAF score и addAF rankscore, на основе которых было сделано предсказание. Порог между оценками, соответствующими повреждающему и толерантному значению предсказания, составляет 0,0692655.",
"SNV", "query", INT />
<@RESOURCE '100081', '1000', 81/>

<@ATTRIBUTE '1001', GR_Protein_add, 10, "BayesDel noAF prediction", "Предсказание BayesDel noAF",
"Predicts the pathogenicity of variants without considering MaxAF: Tolerated (green); Damaging (red). The prediction values correspond to the noAF score and noAF rankscore, based on which the prediction was made. The threshold between scores corresponding to damaging and tolerated predictions is -0.0570105.",
"Предсказание вредоносности вариантов без учёта MaxAF: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным. Значению предсказания соответствуют оценки noAF score и noAF rankscore, на основе которых было сделано предсказание. Порог между оценками, соответствующими повреждающему и толерантному значению предсказания, составляет -0,0570105.",
"SNV", "query", INT />
<@RESOURCE '100181', '1001', 81/>

<@ATTRIBUTE '1002', GR_Protein_add, 10, "PROVEAN score", "Оценка PROVEAN",
"Predicts whether the amino acid substitution affects the biological function of the protein. The results of the prediction are score, rankscore, and prediction: Neutral (green); Damaging (red).",
"Предсказывает, влияет ли аминокислотная замена на биологическую функцию белка. Результатом предсказания являются score, rankscore и prediction: Neutral (нейтральный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100244', '1002', 44/>

<@ATTRIBUTE '1003', GR_Protein_add, 10, "PROVEAN rankscore", "Ранговая оценка PROVEAN",
"Predicts whether the amino acid substitution affects the biological function of the protein. The results of the prediction are score, rankscore, and prediction: Neutral (green); Damaging (red).",
"Предсказывает, влияет ли аминокислотная замена на биологическую функцию белка. Результатом предсказания являются score, rankscore и prediction: Neutral (нейтральный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100344', '1003', 44/>

<@ATTRIBUTE '1004', GR_Protein_add, 10, "PROVEAN prediction", "Предсказание PROVEAN",
"Predicts whether the amino acid substitution affects the biological function of the protein. The results of the prediction are score, rankscore, and prediction: Neutral (green); Damaging (red).",
"Предсказывает, влияет ли аминокислотная замена на биологическую функцию белка. Результатом предсказания являются score, rankscore и prediction: Neutral (нейтральный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100444', '1004', 44/>

<@ATTRIBUTE '1005', GR_Protein_add, 10, "Meta SVM score", "Мета SVM балл",
"A meta-analytic framework using support vector machine methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая метод опорных векторов, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100582', '1005', 82/>

<@ATTRIBUTE '1006', GR_Protein_add, 10, "Meta SVM rankscore", "Мета SVM ранговый балл",
"A meta-analytic framework using support vector machine methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая метод опорных векторов, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100682', '1006', 82/>

<@ATTRIBUTE '1007', GR_Protein_add, 10, "Meta SVM reliability index", "Мета SVM индекс надежности",
"A meta-analytic framework using support vector machine methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая метод опорных векторов, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100782', '1007', 82/>

<@ATTRIBUTE '1008', GR_Protein_add, 10, "Meta SVM prediction", "Мета SVM предсказание",
"A meta-analytic framework using support vector machine methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая метод опорных векторов, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100882', '1008', 82/>

<@ATTRIBUTE '1009', GR_Protein_add, 10, "Meta LR score", "Meta LR score",
"A meta-analytic framework using logistic regression methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая логистическую регрессию, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '100983', '1009', 83/>

<@ATTRIBUTE '1010', GR_Protein_add, 10, "Meta LR rankscore", "Meta LR rankscore",
"A meta-analytic framework using logistic regression methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая логистическую регрессию, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '101083', '1010', 83/>

<@ATTRIBUTE '1011', GR_Protein_add, 10, "Meta LR reliability index", "Meta LR reliability index",
"A meta-analytic framework using logistic regression methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая логистическую регрессию, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '101183', '1011', 83/>

<@ATTRIBUTE '1012', GR_Protein_add, 10, "Meta LR prediction", "Meta LR prediction",
"A meta-analytic framework using logistic regression methodology, integrating 10 scores (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) and the maximum frequency observed in 1000 Genomes populations. The results of the calculations include score, rankscore, reliability index, and prediction: Tolerated (толерантный) is colored green; Damaging (повреждающий) is colored red.",
"Мета-аналитическая структура, использующая логистическую регрессию, интегрирующая 10 шкал (SIFT, PolyPhen-2 HDIV, PolyPhen-2 HVAR, GERP++, MutationTaster, MutationAssessor, FATHMM, LRT, SiPhy, PhyloP) и максимальную частоту, наблюдаемую в популяциях 1000 геномов. Результатом вычислений являются score, rankscore, reliability index, prediction: Tolerated (толерантный) раскрашен зелёным; Damaging (повреждающий) - красным.",
"SNV", "query", INT />
<@RESOURCE '101283', '1012', 83/>

<@ATTRIBUTE '1013', GR_Protein_add, 10, "MutPred rankscore", "MutPred rankscore",
"Classifier of amino acid substitutions as disease-associated or neutral, predicts the molecular cause of disease. The prediction result includes rankscore, score, transcript ID (from UniProtKB), amino acid change, and top 5 features. The higher the score, the more likely the variant is to have a damaging effect.",
"Классификатор аминокислотных замен как ассоциированных с заболеванием или нейтральных, предсказывает молекулярную причину заболевания. Результатом предсказания являются rankscore, score, идентификатор транскрипта (из UniProtKB), аминокислотная замена и основные пять эффектов. Чем больше значение скора, тем более вероятно, что вариант будет иметь повреждающий эффект.",
"SNV", "query", INT />
<@RESOURCE '101342', '1013', 42/>

<@ATTRIBUTE '1014', GR_Protein_add, 10, "MutPred score", "MutPred score",
"Classifier of amino acid substitutions as disease-associated or neutral, predicts the molecular cause of disease. The prediction result includes rankscore, score, transcript ID (from UniProtKB), amino acid change, and top 5 features. The higher the score, the more likely the variant is to have a damaging effect.",
"Классификатор аминокислотных замен как ассоциированных с заболеванием или нейтральных, предсказывает молекулярную причину заболевания. Результатом предсказания являются rankscore, score, идентификатор транскрипта (из UniProtKB), аминокислотная замена и основные пять эффектов. Чем больше значение скора, тем более вероятно, что вариант будет иметь повреждающий эффект.",
"SNV", "query", INT />
<@RESOURCE '101442', '1014', 42/>

<@ATTRIBUTE '1015', GR_Protein_add, 10, "MutPred transcript ID", "Идентификатор транскрипта MutPred",
"Transcript ID from UniProtKB for which the effect of the amino acid substitution on protein function was predicted.",
"Идентификатор транскрипта из базы UniProtKB, для которого было предсказано влияние аминокислотной замены на функцию белка.",
"SNV", "query", INT />
<@RESOURCE '101542', '1015', 42/>

<@ATTRIBUTE '1016', GR_Protein_add, 10, "MutPred amino acid change", "Аминокислотная замена MutPred",
"Amino acid change for which the effect on protein function was predicted.",
"Аминокислотная замена, для которой был предсказан эффект на функцию белка.",
"SNV", "query", INT />
<@RESOURCE '101642', '1016', 42/>

<@ATTRIBUTE '1017', GR_Protein_add, 10, "MutPred top 5 features", "Основные 5 эффектов MutPred",
"The top five effects of the amino acid substitution on protein function. The higher the score, the more likely the variant is to have a damaging effect.",
"Основные пять эффектов аминокислотной замены на функцию белка. Чем больше значение скора, тем более вероятно, что вариант будет иметь повреждающий эффект.",
"SNV", "query", INT />
<@RESOURCE '101742', '1017', 42/>

<@ATTRIBUTE '1018', GR_Protein_add, 10, "MVP rankscore", "MVP rankscore",
"A prediction method that uses a deep residual network to combine large datasets and many correlated predictors. The prediction results are rankscore and score.",
"Метод прогнозирования, который использует глубокую остаточную сеть для объединения больших наборов обучающих данных и множества коррелированных предсказателей. Результатом предсказания являются рангскор и скор.",
"SNV", "query", INT />
<@RESOURCE '101884', '1018', 84/>

<@ATTRIBUTE '1019', GR_Protein_add, 10, "MVP score", "MVP score",
"A prediction method that uses a deep residual network to combine large datasets and many correlated predictors. The prediction results are rankscore and score.",
"Метод прогнозирования, который использует глубокую остаточную сеть для объединения больших наборов обучающих данных и множества коррелированных предсказателей. Результатом предсказания являются рангскор и скор.",
"SNV", "query", INT />
<@RESOURCE '101984', '1019', 84/>

<@ATTRIBUTE '1020', GR_Protein_add, 10, "MPC rankscore", "MPC rankscore",
"A metric for damaging de novo missense mutations based on the identification of subgenomic regions lacking missense variation. The prediction results are rankscore and score. The higher the score, the more likely the variant is pathogenic.",
"Метрика повреждающих de novo миссенс-мутаций, основанная на идентификации субгенных областей, в которых отсутствует миссенс-вариация. Результатом предсказания являются рангскор и скор. Чем больше значение скора, тем более вероятно, что вариант патогенный.",
"SNV", "query", INT />
<@RESOURCE '102085', '1020', 85/>

<@ATTRIBUTE '1021', GR_Protein_add, 10, "MPC score", "MPC score",
"A metric for damaging de novo missense mutations based on the identification of subgenomic regions lacking missense variation. The prediction results are rankscore and score. The higher the score, the more likely the variant is pathogenic.",
"Метрика повреждающих de novo миссенс-мутаций, основанная на идентификации субгенных областей, в которых отсутствует миссенс-вариация. Результатом предсказания являются рангскор и скор. Чем больше значение скора, тем более вероятно, что вариант патогенный.",
"SNV", "query", INT />
<@RESOURCE '102185', '1021', 85/>

<@ATTRIBUTE '1022', GR_Protein_add, 10, "Primate AI score", "Primate AI score",
"Primate AI is a deep residual neural network for classifying the pathogenicity of missense mutations based on common primate variants that are different from human variants. The prediction results are score, rankscore, and prediction: Tolerated is colored green; Damaging is red.",
"Primate AI - это глубокая остаточная нейронная сеть для классификации патогенности миссенс-мутаций, основанная на распространенных вариантах приматов, отличных от человеческих. Результатом предсказания являются скор, рангскор и предсказание: толерантный - раскрашен зелёным; повреждающий - красным.",
"SNV", "query", INT />
<@RESOURCE '102286', '1022', 86/>

<@ATTRIBUTE '1023', GR_Protein_add, 10, "Primate AI rankscore", "Primate AI rankscore",
"Primate AI is a deep residual neural network for classifying the pathogenicity of missense mutations based on common primate variants that are different from human variants. The prediction results are score, rankscore, and prediction: Tolerated is colored green; Damaging is red.",
"Primate AI - это глубокая остаточная нейронная сеть для классификации патогенности миссенс-мутаций, основанная на распространенных вариантах приматов, отличных от человеческих. Результатом предсказания являются скор, рангскор и предсказание: толерантный - раскрашен зелёным; повреждающий - красным.",
"SNV", "query", INT />
<@RESOURCE '102386', '1023', 86/>

<@ATTRIBUTE '1024', GR_Protein_add, 10, "Primate AI prediction", "Primate AI prediction",
"Primate AI is a deep residual neural network for classifying the pathogenicity of missense mutations based on common primate variants that are different from human variants. The prediction results are score, rankscore, and prediction: Tolerated is colored green; Damaging is red.",
"Primate AI - это глубокая остаточная нейронная сеть для классификации патогенности миссенс-мутаций, основанная на распространенных вариантах приматов, отличных от человеческих. Результатом предсказания являются скор, рангскор и предсказание: толерантный - раскрашен зелёным; повреждающий - красным.",
"SNV", "query", INT />
<@RESOURCE '102486', '1024', 86/>

<@ATTRIBUTE '1025', GR_Protein_add, 10, "DEOGEN2 rankscore", "DEOGEN2 rankscore",
"DEOGEN2 is a method that predicts whether a protein-coding variant affects the health of a carrier. The method incorporates heterogeneous information about molecular effects of variants, involved domains, gene relevance, and interactions in which it participates. The prediction result is rankscore.",
"DEOGEN2 - метод, предсказывающий, влияет ли вариант, кодирующий белок, на здоровье человека-носителя. Метод включает разнородную информацию о молекулярных эффектах вариантов, задействованных доменах, релевантности гена и взаимодействиях, в которых он принимает участие. Результатом предсказания является рангскор.",
"SNV", "query", INT />
<@RESOURCE '102587', '1025', 87/>

<@ATTRIBUTE '1026', GR_Protein_add, 10, "DANN rankscore", "DANN rankscore",
"DANN (Domain-Adversarial Neural Network) is a deep neural network retrained based on CADD training data. The results are rankscore and score. The higher the score, the more likely the variant is damaging.",
"DANN (Domain-Adversarial Neural Network) - глубокая нейронная сеть, переобученная на основе данных обучения CADD. Результатом вычислений являются рангскор и скор. Чем больше значение скора, тем более вероятен повреждающий эффект варианта.",
"SNV", "query", INT />
<@RESOURCE '102688', '1026', 88/>

<@ATTRIBUTE '1027', GR_Protein_add, 10, "DANN score", "DANN score",
"DANN (Domain-Adversarial Neural Network) is a deep neural network retrained based on CADD training data. The results are rankscore and score. The higher the score, the more likely the variant is damaging.",
"DANN (Domain-Adversarial Neural Network) - глубокая нейронная сеть, переобученная на основе данных обучения CADD. Результатом вычислений являются рангскор и скор. Чем больше значение скора, тем более вероятен повреждающий эффект варианта.",
"SNV", "query", INT />
<@RESOURCE '102788', '1027', 88/>

<@ATTRIBUTE '1028', GR_Protein_add, 10, "FAtHMM-MKL p-value", "FAtHMM-MKL p-value",
"FAtHMM-MKL (Multiple Kernel Learning) is a machine learning method that integrates functional annotations from ENCODE with hidden Markov models based on nucleotides. The prediction results include p-value, rankscore, and group of features.",
"FAtHMM-MKL (Метод множественного ядерного обучения) - это метод машинного обучения, который объединяет функциональные аннотации из ENCODE со скрытыми марковскими моделями на основе нуклеотидов. Результатом предсказания являются p-value, rankscore, группа признаков.",
"SNV", "query", INT />

<@ATTRIBUTE '1029', GR_Protein_add, 10, "FAtHMM-MKL rankscore", "FAtHMM-MKL rankscore",
"FAtHMM-MKL (Multiple Kernel Learning) is a machine learning method that integrates functional annotations from ENCODE with hidden Markov models based on nucleotides. The prediction results include p-value, rankscore, and group of features.",
"FAtHMM-MKL (Метод множественного ядерного обучения) - это метод машинного обучения, который объединяет функциональные аннотации из ENCODE со скрытыми марковскими моделями на основе нуклеотидов. Результатом предсказания являются p-value, rankscore, группа признаков.",
"SNV", "query", INT />

<@ATTRIBUTE '1030', GR_Protein_add, 10, "FAtHMM-MKL group of features", "FAtHMM-MKL group of features",
"FAtHMM-MKL (Multiple Kernel Learning) is a machine learning method that integrates functional annotations from ENCODE with hidden Markov models based on nucleotides. The prediction results include p-value, rankscore, and group of features.",
"FAtHMM-MKL (Метод множественного ядерного обучения) - это метод машинного обучения, который объединяет функциональные аннотации из ENCODE со скрытыми марковскими моделями на основе нуклеотидов. Результатом предсказания являются p-value, rankscore, группа признаков.",
"SNV", "query", INT />

<@ATTRIBUTE '1031', GR_Protein_add, 10, "FAtHMM-MKL prediction", "FAtHMM-MKL prediction",
"FAtHMM-MKL (Multiple Kernel Learning) is a machine learning method that integrates functional annotations from ENCODE with hidden Markov models based on nucleotides. The prediction results include p-value, rankscore, and group of features.",
"FAtHMM-MKL (Метод множественного ядерного обучения) - это метод машинного обучения, который объединяет функциональные аннотации из ENCODE со скрытыми марковскими моделями на основе нуклеотидов. Результатом предсказания являются p-value, rankscore, группа признаков.",
"SNV", "query", INT />

<@ATTRIBUTE '1032', GR_Protein_add, 10, "FAtHMM-XF p-value", "FAtHMM-XF p-value",
"FAtHMM-XF (eXtended Feature) is a method for predicting pathogenic point mutations in the human genome, which relies on an extensive set of features and is particularly effective in non-coding regions. The prediction results include p-value and rankscore.",
"FAtHMM-XF (Расширенная функция) - это метод прогнозирования патогенных точечных мутаций в геноме человека, который опирается на обширный набор функций и особенно хорошо предсказывает в некодирующих областях. Результатом предсказания являются p-value, rankscore.",
"SNV", "query", INT />

<@ATTRIBUTE '1033', GR_Protein_add, 10, "FAtHMM-XF rankscore", "FAtHMM-XF rankscore",
"FAtHMM-XF (eXtended Feature) is a method for predicting pathogenic point mutations in the human genome, which relies on an extensive set of features and is particularly effective in non-coding regions. The prediction results include p-value and rankscore.",
"FAtHMM-XF (Расширенная функция) - это метод прогнозирования патогенных точечных мутаций в геноме человека, который опирается на обширный набор функций и особенно хорошо предсказывает в некодирующих областях. Результатом предсказания являются p-value, rankscore.",
"SNV", "query", INT />

<@ATTRIBUTE '1034', GR_Protein_add, 10, "FAtHMM-XF prediction", "FAtHMM-XF prediction",
"FAtHMM-XF (eXtended Feature) is a method for predicting pathogenic point mutations in the human genome, which relies on an extensive set of features and is particularly effective in non-coding regions. The prediction results include p-value and rankscore.",
"FAtHMM-XF (Расширенная функция) - это метод прогнозирования патогенных точечных мутаций в геноме человека, который опирается на обширный набор функций и особенно хорошо предсказывает в некодирующих областях. Результатом предсказания являются p-value, rankscore.",
"SNV", "query", INT />

<@ATTRIBUTE '1035', GR_Protein_add, 10, "Eigen rankscore", "Eigen rankscore",
"Eigen - Spectral approach to functional annotation of genetic variants in coding and non-coding regions. The result of the annotation includes rankscore and score.",
"Eigen - Спектральный подход к функциональной аннотации генетических вариантов в кодирующих и некодирующих областях. Результатом аннотации являются rankscore и score.",
"SNV", "query", INT />
<@RESOURCE '103591', '1035', 91/>

<@ATTRIBUTE '1036', GR_Protein_add, 10, "Eigen score", "Eigen score",
"Eigen - Spectral approach to functional annotation of genetic variants in coding and non-coding regions. The result of the annotation includes rankscore and score.",
"Eigen - Спектральный подход к функциональной аннотации генетических вариантов в кодирующих и некодирующих областях. Результатом аннотации являются rankscore и score.",
"SNV", "query", INT />
<@RESOURCE '103691', '1036', 91/>

<@ATTRIBUTE '1037', GR_Protein_add, 10, "Eigen-PC rankscore", "Eigen-PC rankscore",
"Eigen-PC - A simpler meta-score based on the eigen decomposition of the covariance matrix of annotations and the use of the principal eigenvector to weight individual annotations. The evaluation results include rankscore, score, and score in phred scale.",
"Eigen-PC - Более простой мета-скор, который основан на собственном разложении ковариационной матрицы аннотаций и использовании главного собственного вектора для взвешивания отдельных аннотаций. Результатом оценки являются rankscore, score и score in phred scale.",
"SNV", "query", INT />
<@RESOURCE '103791', '1037', 91/>

<@ATTRIBUTE '1038', GR_Protein_add, 10, "Eigen-PC score", "Eigen-PC score",
"Eigen-PC - A simpler meta-score based on the eigen decomposition of the covariance matrix of annotations and the use of the principal eigenvector to weight individual annotations. The evaluation results include rankscore, score, and score in phred scale.",
"Eigen-PC - Более простой мета-скор, который основан на собственном разложении ковариационной матрицы аннотаций и использовании главного собственного вектора для взвешивания отдельных аннотаций. Результатом оценки являются rankscore, score и score in phred scale.",
"SNV", "query", INT />
<@RESOURCE '103891', '1038', 91/>

<@ATTRIBUTE '1039', GR_Protein_add, 10, "Eigen-PC score in phred scale", "Eigen-PC score in phred scale",
"Eigen-PC - A simpler meta-score based on the eigen decomposition of the covariance matrix of annotations and the use of the principal eigenvector to weight individual annotations. The evaluation results include rankscore, score, and score in phred scale.",
"Eigen-PC - Более простой мета-скор, который основан на собственном разложении ковариационной матрицы аннотаций и использовании главного собственного вектора для взвешивания отдельных аннотаций. Результатом оценки являются rankscore, score и score in phred scale.",
"SNV", "query", INT />
<@RESOURCE '103991', '1039', 91/>

<@ATTRIBUTE '1100', GR_Other, 11, "Coding sequence position", "Позиция кодирующей последовательности",
"Position of the reference allele variant in the genomic sequence.",
"Позиция референсного аллеля варианта в геномной последовательности.",
"SNV", "query", INT />
<@RESOURCE '110057', '1100', 57/>

<@ATTRIBUTE '1101', GR_Other, 11, "Protein position", "Позиция белка",
"Position of the reference amino acid in the protein.",
"Позиция референсной аминокислоты в белке.",
"SNV", "query", INT />
<@RESOURCE '110157', '1101', 57/>

<@ATTRIBUTE '1102', GR_Other, 11, "Amino acids", "Аминокислоты",
"Reference amino acid/new amino acid resulting from the substitution.",
"Референсная аминокислота/новая аминокислота, получившаяся в результате замены.",
"SNV", "query", INT />
<@RESOURCE '110257', '1102', 57/>

<@ATTRIBUTE '1103', GR_Other, 11, "Codons", "Кодоны",
"Amino acid substitution written in codons. Example: GAG/CAG = E/Q (glutamic acid -> glutamine).",
"Аминокислотная замена, записанная в виде кодонов. Пример: GAG/CAG = E/Q (глутоминовая кислота -> глутамин).",
"SNV", "query", INT />
<@RESOURCE '110357', '1103', 57/>

<@ATTRIBUTE '1104', GR_Other, 11, "Amino acid position", "Позиция аминокислоты",
"Position of the reference amino acid in the protein.",
"Позиция референсной аминокислоты в белке.",
"SNV", "query", INT />
<@RESOURCE '110457', '1104', 57/>

<@ATTRIBUTE '1105', GR_Other, 11, "APPRIS annotation", "Аннотация APPRIS",
"Annotation of human splice isoforms. For each gene, the principal isoform is selected by combining information on protein structure, functionally important residues, and interspecies alignment data. Possible annotation values: PRINCIPAL1-5 (principal isoforms, where 1 is the most reliable), ALTERNATIVE1 (candidate transcript model that is conservative in at least three tested species not from the primate order), ALTERNATIVE2 (candidate transcript model that is conservative in fewer than three tested species not from the primate order).",
"Аннотация изоформ сплайсинга человека. Для каждого гена изоформа выбирается основной путем объединения информации о структуре белка, функционально важных остатков и данных межвидового выравнивания. Возможные значения аннотации: PRINCIPAL1-5 (основные изоформы, где 1 - наиболее надежная), ALTERNATIVE1 (модель кандидатного транскрипта, являющаяся консервативной как минимум у трех протестированных видов не из отряда приматов), ALTERNATIVE2 (модель кандидатного транскрипта, являющаяся консервативной менее чем у трех протестированных видов не из отряда приматов).",
"SNV", "query", INT />
<@RESOURCE '110592', '1105', 92/>

<@ATTRIBUTE '1106', GR_Other, 11, "Geuvadis EQTL target gene", "Целевой ген eQTL Geuvadis",
"Identifiers of target genes from Ensembl identified through the analysis of expression quantitative trait loci (eQTL) in the GEUVADIS project (Genetic European Variation in Disease).",
"Идентификаторы таргетных генов из Ensembl, выявленных путем анализа локусов количественных признаков экспрессии (eQTL) в проекте GEUVADIS (Genetic European Variation in Disease).",
"SNV", "query", INT />

<@ATTRIBUTE '1107', GR_Other, 11, "ENIGMA clinical significance", "Клиническая значимость ENIGMA",
"Clinical significance of the variant if located in a known or suspected breast and/or ovarian cancer predisposition gene (e.g., BRCA1, BRCA2), determined by experts from the ENIGMA consortium. Clinical significance: Benign is colored dark green; Likely benign is light green; Likely pathogenic is orange; Pathogenic is red; all other values, including mixed clinical significances, are black. Hovering over the field value will show a comment on clinical significance.",
"Клиническая значимость варианта, если он расположен в известном или предполагаемом гене предрасположенности к раку молочной железы и/или яичников (например, BRCA1, BRCA2), определённая экспертами из консорциума ENIGMA. Клиническая значимость: Benign окрашена темно-зеленым; Likely benign - светло-зеленым; Likely pathogenic - оранжевым; Pathogenic - красным; все остальные значения, включая смешанные из разных клинических значимостей - черным. При наведении курсора на значение поля вы увидите комментарий о клинической значимости.",
"SNV", "query", INT />
<@RESOURCE '110794', '1107', 94/>

<@ATTRIBUTE '1200', GR_Custom, 12, "nan", "nan",
"The tab is in the detail information panel if at least one custom annotation was added to the system before the variants in the sample were annotated. The tab contains column values ​​from custom annotations with which the variant was annotated.",
"Вкладка есть на панели детальной информации, если хотя бы одна пользовательская аннотация была добавлена в систему до того, как варианты в образце были проаннотированы. Вкладка содержит значения колонок из пользовательских аннотаций, которыми был проаннотирован вариант.",
"SNV", "query", INT />


<ATTRIBUTE '2001', GR_ACMG_CRITERIA, 9, "PVS1", "PVS1",
"Null variant (nonsense, frameshift, canonical +/−1 or 2 splice sites, initiation codon, single or multi-exon deletion) in a gene where loss of function (LOF) is a known mechanism of disease",
"Null variant (nonsense, frameshift, canonical +/−1 or 2 splice sites, initiation codon, single or multi-exon deletion) in a gene where loss of function (LOF) is a known mechanism of disease",
"SNV", "query", INT />

<ATTRIBUTE '2002', GR_ACMG_CRITERIA, 9, "PS1", "PS1",
"Same amino acid change as a previously established pathogenic variant regardless of nucleotide change",
"Same amino acid change as a previously established pathogenic variant regardless of nucleotide change",
"SNV", "query", INT />

<ATTRIBUTE '2003', GR_ACMG_CRITERIA, 9, "PS2", "PS2",
"De novo (both maternity and paternity confirmed) in a patient with the disease and no family history",
"De novo (both maternity and paternity confirmed) in a patient with the disease and no family history",
"SNV", "query", INT />

<ATTRIBUTE '2004', GR_ACMG_CRITERIA, 9, "PS3", "PS3",
"Well-established in vitro or in vivo functional studies supportive of a damaging effect on the gene or gene product",
"Well-established in vitro or in vivo functional studies supportive of a damaging effect on the gene or gene product",
"SNV", "query", INT />

<ATTRIBUTE '2005', GR_ACMG_CRITERIA, 9, "PS4", "PS4",
"The prevalence of the variant in affected individuals is significantly increased compared to the prevalence in controls",
"The prevalence of the variant in affected individuals is significantly increased compared to the prevalence in controls",
"SNV", "query", INT />

<ATTRIBUTE '2006', GR_ACMG_CRITERIA, 9, "PM1", "PM1",
"Located in a mutational hot spot and/or critical and well-established functional domain (e.g. active site of an enzyme) without benign variation",
"Located in a mutational hot spot and/or critical and well-established functional domain (e.g. active site of an enzyme) without benign variation",
"SNV", "query", INT />

<ATTRIBUTE '2007', GR_ACMG_CRITERIA, 9, "PM2", "PM2",
"Absent from controls (or at extremely low frequency if recessive) (see Table 6) in Exome Sequencing Project, 1000 Genomes or ExAC",
"Absent from controls (or at extremely low frequency if recessive) (see Table 6) in Exome Sequencing Project, 1000 Genomes or ExAC",
"SNV", "query", INT />

<ATTRIBUTE '2008', GR_ACMG_CRITERIA, 9, "PM3", "PM3",
"For recessive disorders, detected in trans with a pathogenic variant",
"For recessive disorders, detected in trans with a pathogenic variant",
"SNV", "query", INT />

<ATTRIBUTE '2009', GR_ACMG_CRITERIA, 9, "PM4", "PM4",
"Protein length changes due to in-frame deletions/insertions in a non-repeat region or stop-loss variants",
"Protein length changes due to in-frame deletions/insertions in a non-repeat region or stop-loss variants",
"SNV", "query", INT />

<ATTRIBUTE '2010', GR_ACMG_CRITERIA, 9, "PM5", "PM5",
"Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before",
"Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before",
"SNV", "query", INT />

<ATTRIBUTE '2011', GR_ACMG_CRITERIA, 9, "PM6", "PM6",
"Assumed de novo, but without confirmation of paternity and maternity",
"Assumed de novo, but without confirmation of paternity and maternity",
"SNV", "query", INT />

<ATTRIBUTE '2012', GR_ACMG_CRITERIA, 9, "PP1", "PP1",
"Co-segregation with disease in multiple affected family members in a gene definitively known to cause the disease",
"Co-segregation with disease in multiple affected family members in a gene definitively known to cause the disease",
"SNV", "query", INT />

<ATTRIBUTE '2013', GR_ACMG_CRITERIA, 9, "PP2", "PP2",
"Missense variant in a gene that has a low rate of benign missense variation and where missense variants are a common mechanism of disease",
"Missense variant in a gene that has a low rate of benign missense variation and where missense variants are a common mechanism of disease",
"SNV", "query", INT />

<ATTRIBUTE '2014', GR_ACMG_CRITERIA, 9, "PP3", "PP3",
"Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc)",
"Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc)",
"SNV", "query", INT />

<ATTRIBUTE '2015', GR_ACMG_CRITERIA, 9, "PP4", "PP4",
"Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology",
"Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology",
"SNV", "query", INT />

<ATTRIBUTE '2016', GR_ACMG_CRITERIA, 9, "PP5", "PP5",
"Reputable source recently reports variant as pathogenic but the evidence is not available to the laboratory to perform an independent evaluation",
"Reputable source recently reports variant as pathogenic but the evidence is not available to the laboratory to perform an independent evaluation",
"SNV", "query", INT />

<ATTRIBUTE '2017', GR_ACMG_CRITERIA, 9, "BA1", "BA1",
"Allele frequency is above 5% in Exome Sequencing Project, 1000 Genomes, or ExAC",
"Allele frequency is above 5% in Exome Sequencing Project, 1000 Genomes, or ExAC",
"SNV", "query", INT />

<ATTRIBUTE '2018', GR_ACMG_CRITERIA, 9, "BS1", "BS1",
"Allele frequency is greater than expected for disorder",
"Allele frequency is greater than expected for disorder",
"SNV", "query", INT />

<ATTRIBUTE '2019', GR_ACMG_CRITERIA, 9, "BS2", "BS2",
"Observed in a healthy adult individual for a recessive (homozygous), dominant (heterozygous), or X-linked (hemizygous) disorder with full penetrance expected at an early age",
"Observed in a healthy adult individual for a recessive (homozygous), dominant (heterozygous), or X-linked (hemizygous) disorder with full penetrance expected at an early age",
"SNV", "query", INT />

<ATTRIBUTE '2020', GR_ACMG_CRITERIA, 9, "BS3", "BS3",
"Well-established in vitro or in vivo functional studies shows no damaging effect on protein function or splicing",
"Well-established in vitro or in vivo functional studies shows no damaging effect on protein function or splicing",
"SNV", "query", INT />

<ATTRIBUTE '2021', GR_ACMG_CRITERIA, 9, "BS4", "BS4",
"Lack of segregation in affected members of a family",
"Lack of segregation in affected members of a family",
"SNV", "query", INT />

<ATTRIBUTE '2022', GR_ACMG_CRITERIA, 9, "BP1", "BP1",
"Missense variant in a gene for which primarily truncating variants are known to cause disease",
"Missense variant in a gene for which primarily truncating variants are known to cause disease",
"SNV", "query", INT />

<ATTRIBUTE '2023', GR_ACMG_CRITERIA, 9, "BP2", "BP2",
"Observed in trans with a pathogenic variant for a fully penetrant dominant gene/disorder; or observed in cis with a pathogenic variant in any inheritance pattern",
"Observed in trans with a pathogenic variant for a fully penetrant dominant gene/disorder; or observed in cis with a pathogenic variant in any inheritance pattern",
"SNV", "query", INT />

<ATTRIBUTE '2024', GR_ACMG_CRITERIA, 9, "BP3", "BP3",
"In-frame deletions/insertions in a repetitive region without a known function",
"In-frame deletions/insertions in a repetitive region without a known function",
"SNV", "query", INT />

<ATTRIBUTE '2025', GR_ACMG_CRITERIA, 9, "BP4", "BP4",
"Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc)",
"Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc)",
"SNV", "query", INT />

<ATTRIBUTE '2026', GR_ACMG_CRITERIA, 9, "BP5", "BP5",
"Variant found in a case with an alternate molecular basis for disease",
"Variant found in a case with an alternate molecular basis for disease",
"SNV", "query", INT />

<ATTRIBUTE '2027', GR_ACMG_CRITERIA, 9, "BP6", "BP6",
"Reputable source recently reports variant as benign but the evidence is not available to the laboratory to perform an independent evaluation",
"Reputable source recently reports variant as benign but the evidence is not available to the laboratory to perform an independent evaluation",
"SNV", "query", INT />

<ATTRIBUTE '2028', GR_ACMG_CRITERIA, 9, "BP7", "BP7",
"A synonymous (silent) variant for which splicing prediction algorithms predict no impact to the splice consensus sequence nor the creation of a new splice site AND the nucleotide is not highly conserved",
"A synonymous (silent) variant for which splicing prediction algorithms predict no impact to the splice consensus sequence nor the creation of a new splice site AND the nucleotide is not highly conserved",
"SNV", "query", INT />


