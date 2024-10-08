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

4. Создайте таблицы приложения в пустой базе данных lims
```
mvn be5:create-db
```

5. Установите зависимости для фронтенда
```sh
npm install
```

6. Для обновления yaml проекта без перезапуска настройте dev.yaml    
[Быстрое обновление dev.yaml](https://github.com/DevelopmentOnTheEdge/be5/wiki/%D0%91%D1%8B%D1%81%D1%82%D1%80%D0%BE%D0%B5-%D0%BE%D0%B1%D0%BD%D0%BE%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-dev.yaml)


## Запуск проекта локально

1. Запуск сервера: ```mvn jetty:run -Djetty.http.port=8200```
2. Запуск фронтенда: ```npm start```
3. Интерфейс в браузере: http://localhost:8888

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

 

### Включить режим разарботки под линуксом (в корневом каталоге)
```sh
echo -e 'paths:\n   lims: ./' > dev.yaml
```

### Накатить скрипты с данными на профиль `test_local`
```sh
mvn -DBE5_PROFILE=test_local be5:data -DBE5_SCRIPT=sample_templates
mvn -DBE5_PROFILE=test_local be5:data -DBE5_SCRIPT=workflows
```


Login: Administrator  
Password: 12345

Если вы воспользовались вторым вариантом запуска тогда нужно настроить debug
[Hot Swapping With Maven, Jetty and IntelliJ](https://gist.github.com/naaman/1053217)
```text
1)In VM Parameters, enter:
  -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=4000
2)add Add New Configuration -> Remote 
```

Для обновления yaml проекта без перезапуска настройте dev.yaml    
[Быстрое обновление dev.yaml](https://github.com/DevelopmentOnTheEdge/be5/wiki/%D0%91%D1%8B%D1%81%D1%82%D1%80%D0%BE%D0%B5-%D0%BE%D0%B1%D0%BD%D0%BE%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-dev.yaml)

### Генерация таблиц и диаграмм для документации
```sh
mvn be5:generate-doc -DBE5_DOC_PATH=../lims-docs/source
```


