FROM eclipse-temurin:21-jdk-alpine@sha256:fd52084c35bbac78890d95d57707d31ffc9d7a3324199e89c06be2a9dc71bfa0 AS builder
WORKDIR /workspace/app

# Copia arquivos de build primeiro para cache
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixa dependências (cache layer)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine@sha256:b48e02e0ea88d7bfb305335df94923bdef394905f4147dccd52e873cf17b1088 AS runtime
WORKDIR /app

# Corrige CVE-2026-111497 (brotli-libs), não coberta pelo digest fixado acima
RUN apk upgrade --no-cache

RUN addgroup -S spring && adduser -S spring -G spring


COPY --from=builder /workspace/app/target/*.jar app.jar

USER spring:spring

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
	CMD wget --no-verbose --tries=1 --spider http://localhost:8084/ || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]