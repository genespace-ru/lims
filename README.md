# Sample Be5 application

Sample project based on https://github.com/DevelopmentOnTheEdge/be5

# Quick start

Download and run mvn install
- https://github.com/DevelopmentOnTheEdge/xmltest
- https://github.com/DevelopmentOnTheEdge/beanexplorer
- https://github.com/DevelopmentOnTheEdge/be-sql
- https://github.com/DevelopmentOnTheEdge/be5

- config connection profile in file: [src/connectionProfiles.local.yaml](https://github.com/QProgS/testBe5app/blob/master/src/connectionProfiles.local.yaml) 
- create file src/profile.local with name of connection, for example: "test_local"
- create database
```sh
mvn be5:create-db
или если нужно только обновить
mvn be5:sync
```

Фронтенд собирается в src/main/webapp/
```sh
//установить зависимости
npm install
//Режим разработки
npm start
//На своей машине быстрее собрать незжатый 
npm run build
//На сервере собирается
npm run build-min
```
Можно также взять готовый фронтенд из https://github.com/DevelopmentOnTheEdge/be5-react/tree/master/dist/compressed
Просто запустите main метод
```java
public class Be5TestAppMain
{
    public static void main(String... args)
    {
        new EmbeddedJetty().run();
    }
}
```
Или 
```sh
mvn clean install jetty:run -Djetty.http.port=8200
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


