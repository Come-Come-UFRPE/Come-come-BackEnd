FROM eclipse-temurin:21-jdk-alpine@sha256:c07bb82e004a166b5d3d63b84ad70e2e822f228505e2e0c0b8dfdc2890d22f13 AS builder
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

FROM eclipse-temurin:21-jre-alpine@sha256:5d7689f47d670aca766b9503a70bccc2d73b4eb9221ae8f714ae958313c4ce70 AS runtime
WORKDIR /app

# Corrige CVE-2026-111497 (brotli-libs), não coberta pelo digest fixado acima
RUN apk upgrade --no-cache

RUN addgroup -S spring && adduser -S spring -G spring


COPY --from=builder /workspace/app/target/*.jar app.jar

USER spring:spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
	CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]