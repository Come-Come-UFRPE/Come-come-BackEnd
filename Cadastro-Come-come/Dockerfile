
FROM eclipse-temurin:21-jdk-alpine AS build

RUN mkdir /app

WORKDIR /app

COPY target/*.jar /app/app.jar

EXPOSE 8081

CMD ["java", "-jar", "/app/app.jar"]