#!/bin/bash

# Переменные окружения для локального запуска
export PORT=8082
export KAFKA_BROKERS=localhost:9092
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DB=cinemaabyss
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres

# Переходим в корень проекта
cd "$(dirname "$0")/.."

# Компилируем и запускаем
./mvnw clean package
java -jar target/events-service-1.0.0.jar