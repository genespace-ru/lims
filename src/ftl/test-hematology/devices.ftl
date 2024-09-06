DELETE FROM device_statuses;
<#macro device_status id, name, description>
INSERT INTO device_statuses(id, name, description, creationdate___, whoinserted___) values(${id}, ${name?str}, ${description?str}, current_timestamp, 'Administator');
</#macro>

<@device_status 1, "availabe", "Устройство доступно для работы." />

DELETE FROM device_types;
<#macro device_type id, name, description>
INSERT INTO device_types(id, name, description, creationdate___, whoinserted___) values(${id}, ${name?str}, ${description?str}, current_timestamp, 'Administator');
</#macro>

<@device_type 1, "Нанофор СПС", "Секвенатор 2-го поколения." />
<@device_type 2, "PacBio", "Секвенатор 3-го поколения." />

DELETE FROM devices;
<#macro device id, sn, title, type, token>
INSERT INTO devices(id, sn, title, typeID, tocken, statusID, creationdate___, whoinserted___)  
            VALUES(${id}, ${sn?str}, ${title?str}, ${type}, ${token?str}, 1, current_timestamp, 'Administator');    
</#macro>

<@device 1, '010108', 'Нанофор СПС - тест', 1, 'secret tocken' />
<@device 2, '010109', 'PacBio  - тест',     2, 'secret tocken' />
