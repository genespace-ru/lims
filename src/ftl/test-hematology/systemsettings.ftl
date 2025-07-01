DELETE FROM systemsettings WHERE section_name = 'lims';

<#macro setting key value>
INSERT INTO systemsettings values('lims', ${key?str}, ${value?str});
</#macro>

<@setting "data_dir",           "C:/projects/genespace/lims-test-hemotology/" />
<@setting "projects_dir",       "C:/projects/genespace/lims-test-hemotology/projects/" />

<@setting "nextflow_dir",       "C:/projects/genespace/nextflow/" />
<@setting "workflow_runs_dir",  "C:/projects/genespace/nextflow/runs/" />

<@setting "nextflow-trace-api", "http://172.30.128.1:8200/nf" />
