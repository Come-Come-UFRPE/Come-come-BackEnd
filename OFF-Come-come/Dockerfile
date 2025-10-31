FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copia arquivos de build primeiro para cache
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixa dependências (cache layer)
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=builder /workspace/app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]