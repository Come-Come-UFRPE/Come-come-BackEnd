FROM eclipse-temurin:25-jdk-alpine@sha256:22edbb1021420ae6e248519d1af07577742314d67d4c60fcbb2e285b7e47fa78 AS builder
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

FROM eclipse-temurin:25-jre-alpine@sha256:69c2a26e9894016e274c880fa542956d005b3755f84db765145185e87405bd89 AS runtime
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