# LIMS - Laboratory Information Management System

Это проект для автоматизации процедур массового анализа результатов секвенирования для массовых рутинных задач (тагетное секвенирование, онкологические данные, мета-геномика и т.п.).

Для анализа данных используются сценарии [nextflow](https://www.nextflow.io/).

Проект построен на основе технологии BeanExplorer version 5 (be5) - https://github.com/DevelopmentOnTheEdge/be5

С проектом связаны еще 2 подпроекта:
- [lims-docs](https://github.com/genespace-ru/lims-docs) - документация по проекту (структура базы данных, диаграммы и т.п.).
Пока это в основном для разработчика.
- [lims-test-hemotology](https://github.com/genespace-ru/lims-test-hemotology) - тестовые данные. 

## Установка проекта локально

1. Создайте базу данных, например lims с пользователем lims, в PostgreSQL. Для этого можно использовать pgAdmin 4.

2. Настроить файл для соединения с базой данных - [connectionProfiles.local.yaml](https://raw.githubusercontent.com/genespace-ru/lims/refs/heads/main/src/connectionProfiles.local.yaml)
В проекте уже есть несколько соединений, которые можно использовать, например:
```
connectionProfiles:
  profiles:
    test_local:
      connectionUrl: jdbc:postgresql://localhost:5434/lims
      username: lims
      password: lims
```

3. указать используемую строку соединения в файле <code>lims/src/profile.local</code>, например:
```
test_local
```
Важно - файл <code>lims/src/profile.local</code> у каждого разработчика свой и не вносится в репозиторий.

4. Установите зависимости для фронтенда
```sh
npm install
```

5. Создайте таблицы приложения в пустой базе данных lims
```
mvn be5:create-db
```

6. Выполните все скрипты для накатывания тестовых данных:
```
mvn be5:data -DBE5_SCRIPT=test-hematology/users
mvn be5:data -DBE5_SCRIPT=test-hematology/project
mvn be5:data -DBE5_SCRIPT=test-hematology/systemsettings
mvn be5:data -DBE5_SCRIPT=test-hematology/analyses
mvn be5:data -DBE5_SCRIPT=test-hematology/devices
mvn be5:data -DBE5_SCRIPT=test-hematology/file_types
mvn be5:data -DBE5_SCRIPT=test-hematology/file_info
mvn be5:data -DBE5_SCRIPT=test-hematology/sample_templates
mvn be5:data -DBE5_SCRIPT=test-hematology/sample_types
mvn be5:data -DBE5_SCRIPT=test-hematology/samples
mvn be5:data -DBE5_SCRIPT=test-hematology/workflow_info
mvn be5:data -DBE5_SCRIPT=attributes/groups
mvn be5:data -DBE5_SCRIPT=attributes/resources
mvn be5:data -DBE5_SCRIPT=attributes/types
mvn be5:data -DBE5_SCRIPT=attributes/attributes
```

## Запуск проекта локально
1. Запуск сервера: ```mvn jetty:run -Djetty.http.port=8200```
2. Запуск фронтенда: ```npm start```
3. Интерфейс в браузере: http://localhost:8888
```
Login: Administrator  
Password: 12345
```

## Полезные комманды

```mvn be5:sync -DBE5_FORCE_UPDATE=true```
<br/>Обновляет структуру базы данных по файлам yaml. 

```mvn be5:data -DBE5_SCRIPT=attributes/attributes```
<br/>Выполняет заданный скрипт, здесь attributes/attributes.ftl
<br/>Важно:
- скрипт имеет расширение ```.ftl```, но оно при запуске не указывается,
- скрипт должен быть указан в проекте (```project.yaml```):
```
  scripts:
  - Post-db
  - dictionaries
  - test-hematology/users
  - test-hematology/project
...
```

### Генерация таблиц и диаграмм для документации
```sh
mvn be5:generate-doc -DBE5_DOC_PATH=../lims-docs/source
```
После этого в проекте lims-docs
```sh
make.html
```

## Восстановление базы из дампа на продуктивном сервере

```sh
docker run -v ./lims:/lims -it --rm postgres:16 pg_restore -f /lims/lims-10-18.sql /lims/lims-10-18
```

```sh
ssh -L 5435:localhost:5435 iap
```

На сервере
```sh
docker exec -it postgres-lims bash
dropdb -h localhost -U lims  lims
createdb --encoding='utf-8' --lc-collate='en_US.UTF-8' --lc-ctype='en_US.UTF-8' --template=template0 -h localhost -U lims lims
```

```sh
psql -h localhost -p 5435 -U lims -d lims < Downloads/lims/lims-10-18.sql 
```
