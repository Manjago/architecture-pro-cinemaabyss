# --- Этап 1: Сборка (Builder) ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests


# --- Этап 2: Исполнение (Runtime) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/events-service-*.jar app.jar

EXPOSE 8082 

CMD ["java", "-jar", "app.jar"]
