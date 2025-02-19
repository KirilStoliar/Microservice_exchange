# Microservice exchange

Microservice_exchange - микросервис, который состоит из двух API: банк и клиент. Клиент может получать:
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
Приложение покрыто тестами > 70%.

## Варианты запуска
```sh
docker-compose up --build
```
Swagger: http://localhost:8080/swagger-ui/index.html#/
Либо тестирование запросами через Postman. Запросы описаны в BankController и ClientController.

При отсутствии Docker приложение можно запустить в выбранной вами среде разработки через MicroserviceExchangeApplication,  при этом изменив данные для подключения к базе данных postgres в файле resources/application.yml.
