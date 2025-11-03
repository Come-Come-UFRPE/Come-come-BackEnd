FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Vamos usar o dos2unix que funcionou para os scripts
RUN apk add --no-cache dos2unix

# Copia arquivos de build
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Corrige os scripts do Maven (line endings)
RUN dos2unix mvnw
RUN dos2unix .mvn/wrapper/maven-wrapper.properties
RUN chmod +x mvnw

# Baixa dependências (cache layer)
RUN ./mvnw dependency:go-offline

# Copia o código-fonte
COPY src src

# Limpa os line endings do properties (não custa)
RUN dos2unix src/main/resources/application.properties

# --- NOVA CORREÇÃO ---
# Diz ao Maven para ler os ficheiros de recursos como Windows-1252
RUN ./mvnw -Dproject.build.sourceEncoding=Windows-1252 clean package -DskipTests

# --- Segundo Estágio (Runtime) ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /workspace/app/target/*.jar app.jar

USER spring:spring

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]