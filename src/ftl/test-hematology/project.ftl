DELETE FROM projects WHERE ID=1 AND name='test-hematology';

INSERT INTO projects VALUES(1, 'test-hematology', 'Тестовый проект по тагетному секвенированию для гематологии',
CURRENT_TIMESTAMP, null, 'Administrator', null); 
