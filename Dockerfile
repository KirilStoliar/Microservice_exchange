FROM openjdk:17

LABEL maintainer="kiss200486@mail.ru"

WORKDIR /app

COPY target/Microservice_exchange-1.0.jar ./microservice.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/microservice.jar"]
