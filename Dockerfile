# --- Etapa 1: Compilação (Usa o Maven com Java 17) ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Execução Leve (Runtime do Java 17 Slim) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# --- Fiação mestre de controle de Memória da JVM (256MB) ---
ENV JAVA_OPTS="-Xmx256M -Xms128M"

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

