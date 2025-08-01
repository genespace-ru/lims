DELETE FROM resource_types;
DELETE FROM resources;
DELETE FROM resource_recommendations;

<#macro RESOURCE_TYPE id, title, description, comment="null">
    INSERT INTO resource_types(id, title, description, comment) 
    VALUES( ${id}, ${title?str}, ${description?str}, ${comment});
</#macro>

<#macro RESOURCE id, type, title, version, description, url, license, comment="null">
    INSERT INTO resources(id, typeID, title, version, description, url, license, comment) 
    VALUES( ${id}, ${type}, ${title?str}, ${version?str}, ${description?str}, ${url?str}, ${license?str}, ${comment});
</#macro>

<#macro RECOMMENDATION id, resource, recommendation, comment>
    INSERT INTO resource_recommendations(id, resource, recommendation, comment) 
    VALUES( ${id}, ${resource}, ${recommendation}, ${comment});
</#macro>

<@RESOURCE_TYPE 1, "database", "База данных" />
<@RESOURCE_TYPE 2, "program", "Программа" />
<@RESOURCE_TYPE 3, "recommendation", "Клинические рекомендации" />
<@RESOURCE_TYPE 4, "format", "Формат данных" />
<@RESOURCE_TYPE 5, "jupyter", "Jupyter ноутбук" />

<#assign DATABASE            = 1 /> 
<#assign PROGRAM             = 2 /> 
<#assign RECOMMENDATION_TYPE = 3 /> 
<#assign FORMAT              = 4 /> 
<#assign JUPYTER             = 5 /> 

<@RESOURCE 1, RECOMMENDATION_TYPE, "Руководство по интерпретации", "редакция 2024, версия 3",
"Руководства по интерпретации данных последовательности ДНК человека, полученных методами массового параллельного секвенирования (МПСMPS)."
"https://docs.google.com/document/d/1_tFCEzjARo3dx3cTWFa-4BPLgOpf8Lh7xYpr2ncp5bw", "" />

<@RESOURCE 2, DATABASE, "GnomAD", " v3.1.2",
"Расширенная база данных геномных вариантов, основанная на базе Exome Aggregation Consortium."
"http://gnomad.broadinstitute.org/", "" />
<@RECOMMENDATION 1, 2, 1, "'Популяционные ресурсы'" />

<@RESOURCE 3, DATABASE, "GnomAD", " v4.0.0",
"Расширенная база данных геномных вариантов, основанная на базе Exome Aggregation Consortium."
"http://gnomad.broadinstitute.org/", "" />
<@RECOMMENDATION 2, 3, 1, "'Популяционные ресурсы. Мы не рекомендуем использовать значения GnomAD v4.0.0, так как имеются сообщения об ошибках в частоте аллелей DOI: 10.1101/gr.277908.123'" />

<@RESOURCE 5, DATABASE, "Exome Aggregation Consortium", "",
"База данных вариантов, найденных при проведении экзомного секвенирования у 61,486 неродственных индивидуумов, являющихся участниками различных болезнь-специфичных и популяционных генетических исследований. Лица с наследственными заболеваниями, проявляющимися в детстве, были исключены из выборки."
"http://exac.broadinstitute.org/", "" />
<@RECOMMENDATION 4, 5, 1, "'Популяционные ресурсы. Устарело.'" />

<@RESOURCE 6, DATABASE, "dbSNP", "",
"База данных коротких генетических вариантов (как правило, ≤50 п.о.), собранных из различных источников. Наряду с популяционными вариантами содержит и множество патогенных вариантов."
"http://www.ncbi.nlm.nih.gov/snp", "" />
<@RECOMMENDATION 5, 6, 1, "'Популяционные ресурсы.'" />

<@RESOURCE 7, DATABASE, "dbVar", "",
"База данных структурных вариантов (как правило, >50 п.о.), составленная из многих источников."
"http://www.ncbi.nlm.nih.gov/dbvar", "" />
<@RECOMMENDATION 6, 7, 1, "'Популяционные ресурсы.'" />

<@RESOURCE 8, DATABASE, "Mitomap", "",
"База данных вариантов митохондриального генома."
"https://www.mitomap.org/foswiki/bin/view/MITOMAP/WebHome", "" />
<@RECOMMENDATION 7, 8, 1, "'Доступ в РФ ограничен.'" />

<@RESOURCE 9, DATABASE, "OMIM", "",
"База данных генов человека и генетических состояний, которая содержит репрезентативную выборку вариантов, ассоциированных с заболеваниями."
"http://www.omim.org/", "" />
<@RECOMMENDATION 8, 9, 1, "'Клинические ресурсы.'" />

<@RESOURCE 10, DATABASE, "Human Gene Mutation Database", "",
"База данных аннотированных вариантов, опубликованных в литературе. В базе встречаются варианты, необходимо уточнять клиническую значимость по литературным данным."
"http://www.hgmd.cf.ac.uk/ac/index.php", "Доступ к основной части контента требует оплаты." />
<@RECOMMENDATION 9, 10, 1, "'Регистрация и доступ к HGMD в РФ ограничен.'" />

<@RESOURCE 11, DATABASE, "ClinVar", "",
"База данных генетических вариантов с описанием патогенности."
"http://clinvar.com/", "" />
<@RECOMMENDATION 10, 11, 1, "'Клинические ресурсы. Использовать с учетом правил.'" />

<@RESOURCE 12, DATABASE, "GenCC", "",
"Курируемая база генов моногенных патологий с указанием силы ассоциаций генотип-фенотип."
"https://thegencc.org/", "" />
<@RECOMMENDATION 11, 12, 1, "'Клинические ресурсы.'" />

<@RESOURCE 13, DATABASE, "ClinGen", "",
"Курируемая база с описанием клинической значимости генов, вариантов и фенотипов."
"https://clinicalgenome.org/", "" />
<@RECOMMENDATION 12, 13, 1, "'Клинические ресурсы.'" />

<@RESOURCE 14, DATABASE, "ClinGen Variants", "",
"Курируемая база данных вариантов с определением их патогенности."
"https://erepo.genome.network/evrepo/ui", "" />
<@RECOMMENDATION 13, 14, 1, "'Клинические ресурсы.'" />

<@RESOURCE 15, DATABASE, "LOVD", "",
"Платформа для создания баз данных вариантов генов. Необходимо уточнять дату последней курации, куратора, число вариантов для каждого отдельного гена."
"https://www.lovd.nl/", "" />
<@RECOMMENDATION 14, 15, 1, "'Клинические ресурсы. Доступ в РФ ограничен.'" />

<@RESOURCE 16, DATABASE, "Human Genome Variation Society", "",
"Сайт общества по изучению вариаций генома человека, которое создало несколько тысяч баз данных, предоставляющих варианты аннотации нуклеотидных замен."
"http://www.hgvs.org/dblist/dblist.html", "" />
<@RECOMMENDATION 15, 16, 1, "'Клинические ресурсы.'" />

<@RESOURCE 17, DATABASE, "Leiden Open Variation Database", "",
"Система, содержащая базы данных с аннотациями нуклеотидных замен."
"http://www.lovd.nl", "" />
<@RECOMMENDATION 16, 17, 1, "'Клинические ресурсы.'" />

<@RESOURCE 18, DATABASE, "DECIPHER", "",
"Молекулярно-цитогенетическая база данных, связывающая геномные данные с фенотипом через геномный браузер Ensembl."
"https://decipher.sanger.ac.uk/", "" />
<@RECOMMENDATION 17, 18, 1, "'Клинические ресурсы.'" />

<@RESOURCE 19, DATABASE, "DGV", "",
"База данных структурных вариантов с описанием их клинической значимости."
"http://dgv.tcag.ca/dgv/app/home", "" />
<@RECOMMENDATION 18, 19, 1, "'Клинические ресурсы.'" />

<@RESOURCE 20, DATABASE, "ClinGen CNV", "",
"Гаплочувствительность/гаплонедостаточность CNV."
"https://ftp.clinicalgenome.org/", "" />
<@RECOMMENDATION 19, 20, 1, "'Клинические ресурсы.'" />

<@RESOURCE 21, DATABASE, "PharmGKB", "",
"Веб-ресурс, содержащий информацию о влиянии генетических вариаций на лекарственную реакцию, а также список одобренных FDA лекарственных средств."
"https://www.pharmgkb.org/", "" />
<@RECOMMENDATION 20, 21, 1, "'Фармакогеномика.'" />

<@RESOURCE 22, DATABASE, "EMA", "",
"Список одобренных EMA лекарственных средств."
"https://www.ema.europa.eu/en/homepage", "" />
<@RECOMMENDATION 21, 22, 1, "'Фармакогеномика.'" />

<@RESOURCE 23, DATABASE, "AACT", "",
"Список лекарственных средств, проходящих клинические испытания."
"https://clinicaltrials.gov/", "" />
<@RECOMMENDATION 22, 23, 1, "'Фармакогеномика.'" />

<@RESOURCE 24, DATABASE, "PanelApp", "",
"Курируемые панели генов."
"https://panelapp.genomicsengland.co.uk/", "" />
<@RECOMMENDATION 23, 24, 1, "'Клинические ресурсы.'" />

<@RESOURCE 25, DATABASE, "HPO", "",
"Создание панелей генов на основе задаваемых симптомов."
"http://charite.github.io/compbio-onto.html", "" />
<@RECOMMENDATION 24, 25, 1, "'Клинические ресурсы.'" />

<@RESOURCE 26, DATABASE, "GTEx", "",
"Данные тканеспецифического секвенирования РНК на уровне экзонов с информацией об альтернативном сплайсинге и пропуске экзонов."
"https://www.gtexportal.org/home/", "" />
<@RECOMMENDATION 25, 26, 1, "'Клинические ресурсы.'" />

<@RESOURCE 27, DATABASE, "The Human Protein Atlas", "",
"Ресурс данных об экспрессии РНК из 40 типов тканей человека, включая данные GTEx и консорциума FANTOM."
"https://www.proteinatlas.org/", "" />
<@RECOMMENDATION 26, 27, 1, "'Клинические ресурсы.'" />

<@RESOURCE 28, DATABASE, "DrugBank", "",
"База данных о фармо-мультиомике (фармакогеномика, транскриптомика, метаболомика, протеомика)."
"https://go.drugbank.com/", "" />
<@RECOMMENDATION 27, 28, 1, "'Фармакогеномика.'" />

<@RESOURCE 29, DATABASE, "MANE", "",
"Ресурс полногеномных референсных последовательностей."
"https://ftp.ncbi.nlm.nih.gov/refseq/MANE/", "" />
<@RECOMMENDATION 28, 29, 1, "'Клинические ресурсы.'" />

<@RESOURCE 30, DATABASE, "RefSeqGene", "",
"Ресурс референсных последовательностей клинически релевантных генов."
"http://www.ncbi.nlm.nih.gov/refseq/rsg/", "" />
<@RECOMMENDATION 29, 30, 1, "'Клинические ресурсы.'" />

<@RESOURCE 31, DATABASE, "MitoMap", "",
"Исправленная кембриджская референсная последовательность митохондриальной ДНК человека."
"http://www.mitomap.org/MITOMAP/", "" />
<@RECOMMENDATION 30, 31, 1, "'Клинические ресурсы.'" />

<@RESOURCE 32, PROGRAM, "ConSurf", "",
"Эволюционная консервативность."
"http://consurftest.tau.ac.il/", "" />
<@RECOMMENDATION 31, 32, 1, "'Компьютерные (in silico) предсказательные программы.'" />

<@RESOURCE 33, PROGRAM, "FATHMM", "",
"Эволюционная консервативность."
"http://fathmm.biocompute.org.uk/", "" />
<@RECOMMENDATION 32, 33, 1, "'Компьютерные (in silico) предсказательные программы.'" />

<@RESOURCE 34, PROGRAM, "MutationAssessor", "",
"Эволюционная консервативность."
"http://mutationassessor.org/", "" />
<@RECOMMENDATION 33, 34, 1, "'Компьютерные (in silico) предсказательные программы.'" />

<@RESOURCE 35, PROGRAM, "PANTHER", "",
"Эволюционная консервативность."
"http://www.pantherdb.org/tools/snpScoreForm.jsp", "" />
<@RECOMMENDATION 34, 35, 1, "'Компьютерные (in silico) предсказательные программы.'" />

<@RESOURCE 36, PROGRAM, "PhD-SNP", "",
"Эволюционная консервативность."
"http://snps.biofold.org/phd-snp/phd-snp.html", "" />
<@RECOMMENDATION 35, 36, 1, "'Компьютерные (in silico) предсказательные программы.'" />

<@RESOURCE 37, PROGRAM, "SIFT", "",
"Эволюционная консервативность."
"http://sift.jcvi.org/", "" />
<@RECOMMENDATION 36, 37, 1, "'Компьютерные (in silico) предсказательные программы.'" />

<@RESOURCE 38, PROGRAM, "SNPs&GO", "",
"Структура/функция белка."
"http://snps-and-go.biocomp.unibo.it/snps-and-go/", "" />
<@RECOMMENDATION 37, 38, 1, "'Компьютерные (in silico) предсказательные программы.'" />

<@RESOURCE 39, PROGRAM, "Align GVGD", "",
"Структура/функция белка и эволюционная консервативность."
"http://agvgd.hci.utah.edu/agvgd_input.php", "" />
<@RECOMMENDATION 38, 39, 1, "'Миссенс замены.'" />

<@RESOURCE 40, PROGRAM, "MAPP", "",
"Структура/функция белка и эволюционная консервативность."
"http://mendel.stanford.edu/SidowLab/downloads/MAPP/index.html", "" />
<@RECOMMENDATION 39, 40, 1, "'Миссенс замены.'" />

<@RESOURCE 41, PROGRAM, "MutationTaster", "",
"Структура/функция белка и эволюционная консервативность."
"http://www.mutationtaster.org/", "" />
<@RECOMMENDATION 40, 41, 1, "'Миссенс замены.'" />

<@RESOURCE 42, PROGRAM, "MutPred", "",
"Структура/функция белка и эволюционная консервативность."
"http://mutpred.mutdb.org/", "" />
<@RECOMMENDATION 41, 42, 1, "'Миссенс замены.'" />

<@RESOURCE 43, PROGRAM, "PolyPhen-2", "",
"Структура/функция белка и эволюционная консервативность."
"http://genetics.bwh.harvard.edu/pph2/", "" />
<@RECOMMENDATION 42, 43, 1, "'Миссенс замены.'" />

<@RESOURCE 44, PROGRAM, "PROVEAN", "",
"Выравнивание и измерение сходства между последовательностью варианта и последовательностью гомологичных белков."
"http://provean.jcvi.org/index.php", "" />
<@RECOMMENDATION 43, 44, 1, "'Миссенс замены.'" />

<@RESOURCE 45, PROGRAM, "SIFT", "",
"Выравнивание и измерение сходства между последовательностью варианта и последовательностью гомологичных белков."
"http://sift.jcvi.org", "" />
<@RECOMMENDATION 44, 45, 1, "'Миссенс замены.'" />

<@RESOURCE 46, PROGRAM, "nsSNPAnalyzer", "",
"Выравнивание множества последовательностей и анализ структуры белка."
"http://snpanalyzer.uthsc.edu/", "" />
<@RECOMMENDATION 45, 46, 1, "'Миссенс замены.'" />

<@RESOURCE 47, PROGRAM, "Condel", "",
"Объединяет SIFT, PolyPhen-2 и MutationAssessor."
"http://bg.upf.edu/fannsdb/", "" />
<@RECOMMENDATION 46, 47, 1, "'Миссенс замены.'" />

<@RESOURCE 48, PROGRAM, "MetaRNN", "",
"Рекуррентная нейронная сеть для несинонимичных/индел вариантов без сдвига рамки считывания."
"https://github.com/Chang-Li2019/MetaRNN", "" />
<@RECOMMENDATION 47, 48, 1, "'Миссенс замены.'" />

<@RESOURCE 49, PROGRAM, "GeneSplicer", "",
"Модели Маркова."
"http://ccb.jhu.edu/software/genesplicer/", "" />
<@RECOMMENDATION 48, 49, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 50, PROGRAM, "Human Splicing Finder", "",
"Положение варианта."
"http://www.umd.be/HSF/", "" />
<@RECOMMENDATION 49, 50, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 51, PROGRAM, "MaxEntScan", "",
"Принцип максимальной энтропии."
"http://genes.mit.edu/burgelab/maxent/Xmaxentscanscoreseq.html", "" />
<@RECOMMENDATION 50, 51, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 52, PROGRAM, "NetGene2", "",
"Нейронные сети."
"http://www.cbs.dtu.dk/services/NetGene2/", "" />
<@RECOMMENDATION 51, 52, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 53, PROGRAM, "NNSplice", "",
"Нейронные сети."
"http://www.fruitfly.org/seq_tools/splice.html", "" />
<@RECOMMENDATION 52, 53, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 54, PROGRAM, "ASSP", "",
"Нейронные сети."
"http://wangcomputing.com/assp/", "" />
<@RECOMMENDATION 53, 54, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 55, PROGRAM, "FSPLICE", "",
"Видоспецифичный предиктор сайтов сплайсинга, основанный на модели весовой матрицы."
"http://www.softberry.com/berry.phtml?topic=fsplice&group=programs&subgroup=gfind", "" />
<@RECOMMENDATION 54, 55, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 56, PROGRAM, "SpliceAI", "",
"Глубокая нейронная сеть."
"https://spliceailookup.broadinstitute.org/", "" />
<@RECOMMENDATION 55, 56, 1, "'Изменения в сайтах сплайсинга.'" />

<@RESOURCE 57, PROGRAM, "VEP", "",
"Аннотатор."
"https://www.ensembl.org/info/docs/tools/vep/index.html", "" />

<@RESOURCE 58, PROGRAM, "deepvariant", "",
"Variant caller  на основе глубокого обучения, который выполняет выровненные чтения."
"https://github.com/google/deepvariant", "" />

<@RESOURCE 59, FORMAT, "VCF", "4.2",
"The Variant Call Format is a standard text file format used for storing gene sequence variations."
"https://samtools.github.io/hts-specs/VCFv4.2.pdf", "" />

<@RESOURCE 60, DATABASE, "COSMIC", "",
"База данных соматических мутаций, связанных с раком."
"", "" />

<@RESOURCE 61, DATABASE, "Franklin", "",
"Платформа для анализа вариантов с клиническими аннотациями."
"", "" />

<@RESOURCE 62, PROGRAM, "UCSC", "",
"Университет Калифорнии, Санта-Круз: геномный браузер с обширными аннотациями."
"", "" />

<@RESOURCE 63, PROGRAM, "RUSeq", "",
"Платформа для анализа данных RNA-Seq."
"", "" />

<@RESOURCE 64, DATABASE, "gnomAD", "",
"Геномная база данных аллельных частот."
"", "" />

<@RESOURCE 65, DATABASE, "Ensembl", "",
"Геномная база данных, предоставляющая аннотации и геномные ресурсы."
"", "" />

<@RESOURCE 66, DATABASE, "UniProt", "",
"База данных белков с аннотацией белковых последовательностей и функциональной информацией."
"", "" />

<@RESOURCE 67, DATABASE, "NCBI", "",
"Национальный центр биотехнологической информации: предоставляет ресурсы геномных данных."
"", "" />

<@RESOURCE 68, DATABASE, "Orphanet", "",
"База данных редких заболеваний и их генетических причин."
"", "" />

<@RESOURCE 69, DATABASE, "HGVS", "",
"Стандарты генетической нотации для обозначения мутаций и генетических вариантов."
"", "" />

<@RESOURCE 70, DATABASE, "OMIM", "",
"Онлайн база данных наследственных заболеваний человека."
"", "" />

<@RESOURCE 71, DATABASE, "ClinVar", "",
"База данных вариантов с клиническими значениями и их аннотациями."
"", "" />

<@RESOURCE 72, DATABASE, "1000 G", "",
"База данных 1000 Genomes Project, предоставляющая информацию о частотах аллелей."
"", "" />

<@RESOURCE 73, PROGRAM, "GERP", "",
"Оценка филогенетической консервативности сайтов."
"", "" />

<@RESOURCE 74, PROGRAM, "PhyloP", "",
"Оценка эволюционной консервативности на основе филогенетических деревьев."
"", "" />

<@RESOURCE 75, PROGRAM, "PhastCons", "",
"Оценка консервативности основана на сравнении нескольких видов."
"", "" />

<@RESOURCE 76, PROGRAM, "SiPhy", "",
"Оценка для консервативных элементов, основанная на статистических моделях."
"", "" />

<@RESOURCE 77, PROGRAM, "BStatistic", "",
"Баесовская оценка консервативности сайтов."
"", "" />

<@RESOURCE 78, PROGRAM, "LRT", "",
"Тест правдоподобия для оценки воздействия мутаций на функциональность белка."
"", "" />

<@RESOURCE 79, PROGRAM, "FATHMM", "",
"Прогнозатор функциональных эффектов на основе скрытых марковских моделей."
"", "" />

<@RESOURCE 80, PROGRAM, "CADD", "",
"Оценка для прогнозирования патогенности на основе комбинированных данных."
"", "" />

<@RESOURCE 81, PROGRAM, "BayesDe", "",
"Баесовская модель для предсказания воздействия мутаций."
"", "" />

<@RESOURCE 82, PROGRAM, "Meta SVM", "",
"Прогнозатор мутаций с использованием метаанализа и метода опорных векторов."
"", "" />

<@RESOURCE 83, PROGRAM, "Meta LR", "",
"Прогнозатор мутаций с использованием метаанализа и логистической регрессии."
"", "" />

<@RESOURCE 84, PROGRAM, "MVP", "",
"Оценка патогенности мутаций на основе вероятностных моделей."
"", "" />

<@RESOURCE 85, PROGRAM, "MPC", "",
"Оценка мутационной консервативности, используемая для предсказания патогенности."
"", "" />

<@RESOURCE 86, PROGRAM, "Primate AI", "",
"Прогнозатор воздействия мутаций на основе глубокого обучения и приматов."
"", "" />

<@RESOURCE 87, PROGRAM, "DEOGEN2", "",
"Прогнозатор патогенности мутаций с использованием машинного обучения."
"", "" />

<@RESOURCE 88, PROGRAM, "DANN", "",
"Прогнозатор патогенности на основе нейронных сетей."
"", "" />

<@RESOURCE 89, PROGRAM, "FATHMM-MKL", "",
"Прогнозатор патогенности, использующий множество слоев и скрытые марковские модели."
"", "" />

<@RESOURCE 90, PROGRAM, "FATHMM-XF", "",
"Прогнозатор, ориентированный на функциональные эффекты для некодирующих мутаций."
"", "" />

<@RESOURCE 91, PROGRAM, "Eigen", "",
"Оценка вероятности патогенности на основе статистических моделей и аннотаций."
"", "" />

<@RESOURCE 92, PROGRAM, "APPRIS", "",
"Программа для аннотации транскриптов с учетом их функциональной значимости."
"", "" />

<@RESOURCE 93, PROGRAM, "Geuvadis", "",
"База данных eQTL (экспрессия количественных признаков), ассоциированных с определенными генами."
"", "" />

<@RESOURCE 94, PROGRAM, "ENIGMA", "",
"Оценка мутаций, связанная с рисками онкологических заболеваний."
"", "" />