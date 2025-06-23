DELETE FROM projects WHERE ID=1 AND name='test-hematology';

INSERT INTO projects( ID, name, description, creationDate___, whoInserted___ )
VALUES(1, 'test-hematology', 'Тестовый проект по тагетному секвенированию для гематологии', CURRENT_TIMESTAMP, 'Administrator'); 

DELETE FROM runs WHERE ID=1;
INSERT INTO runs(ID, project_id, name, status, data, creationDate___, whoInserted___)
    VALUES(1, 1, 'index_lib_15%phix_4pM', 'completed', 
           pg_read_file('C:/projects/genespace/lims-test-hemotology/projects/test-hematology/SampleSheet.yaml'),
           CURRENT_TIMESTAMP, 'Administrator')
