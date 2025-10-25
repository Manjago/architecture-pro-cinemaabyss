# --- Этап 1: Сборка (Builder) ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests


# --- Этап 2: Исполнение (Runtime) ---
FROM eclipse-temurin:21-jre-alpine

# Install necessary runtime dependencies AND bash (for wait-for-it.sh)
RUN apk --no-cache add ca-certificates tzdata bash

WORKDIR /app

COPY --from=builder /app/target/proxy-service-*.jar app.jar

EXPOSE 8000 

COPY wait-for-it.sh /usr/local/bin/wait-for-it.sh
RUN chmod +x /usr/local/bin/wait-for-it.sh

CMD ["java", "-jar", "app.jar"]
