DELETE FROM systemsettings WHERE 'section_name' = 'lims';

<#macro setting key value>
INSERT INTO systemsettings values('lims', ${key?str}, ${value?str});
</#macro>

<@setting "data_dir",     "C:/projects/genespace/lims-test-hemotology"/>
<@setting "projects_dir", "C:/projects/genespace/lims-test-hemotology/projects"/>
 