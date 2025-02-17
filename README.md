# REST API

Микросервис состоит из двух API: банк и клиент. Клиент может получать:
* список всех транзакций
* транзакцию по ID
* список транзакций, которые превысили лимит.

Банк устанавливает месячный лимит на транзакции и добавляет успешные транзакции в базу данных.

## Используемые технологии: 
- Java 17, Maven
- PostgreSQL, Liquibase, Spring Data
- Spring Boot 3.4.2
- Spring Boot Test, JUnit5, Mockito, H2 database
- Swagger
- Docker
- Logger slf4j, Lombok

В качестве получения данных курсов валют используется сайт https://twelvedata.com/. При остустствии данных стоит ввести свой API-ключ в resources/application.yml.

## Описание запуска

Приложение можно запустить в контейнере Docker. Для этого следует запустить docker-compose.yml. После запуска в адресной строке браузера вводим: http://localhost:8080/swagger-ui/index.html#/ либо тестировать запросы через Postman.

При отсутствии Docker приложение можно запустить в выбранной вами среде разработки через MicroserviceExchangeApplication,  при этом изменив данные для подключения к базе данных postgres в файле resources/application.yml. После запуска вводим в адресной строке браузера: http://localhost:8080/swagger-ui/index.html#/.
