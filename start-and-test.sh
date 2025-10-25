#!/bin/bash
# Этот скрипт автоматизирует запуск всего проекта и прогон тестов.
# Он гарантирует, что все компоненты будут работать в предсказуемой сети.

set -e # Прерывать выполнение при любой ошибке

# --- Конфигурация ---
# Имя проекта. Docker Compose будет использовать его как префикс.
PROJECT_NAME="cinemaabyss"
# Имя сети внутри docker-compose.yml
INTERNAL_NETWORK_NAME="cinemaabyss-network"
# Конечное имя сети, которое сгенерирует Docker
DOCKER_NETWORK_NAME="${PROJECT_NAME}_${INTERNAL_NETWORK_NAME}"

# --- Шаг 1: Запуск окружения ---
echo "🚀 Starting services with project name '$PROJECT_NAME'..."
docker-compose -p "$PROJECT_NAME" up --build -d

echo "⏳ Waiting for services to become ready..."
# Простая пауза. В идеале здесь нужен более умный health check,
# но для учебного проекта этого достаточно, чтобы все "устаканилось".
sleep 15 
echo "✅ Services should be up."

# --- Шаг 2: Запуск тестов ---
echo "🧪 Running API tests in Docker..."
cd tests/postman

# Создаем временную копию скрипта, чтобы не менять исходник в git
cp run-tests.sh run-tests-temp.sh

# Заменяем захардкоженное имя сети на наше, сгенерированное
# `sed -i` может работать по-разному в Linux и macOS, этот вариант более кросс-платформенный
sed "s/--network=[^ ]*/--network=$DOCKER_NETWORK_NAME/" run-tests-temp.sh > run-tests-temp.sh.tmp && mv run-tests-temp.sh.tmp run-tests-temp.sh

chmod +x run-tests-temp.sh

# Запускаем тесты через временный скрипт
./run-tests-temp.sh -d -e docker

# Удаляем временный файл
rm run-tests-temp.sh

cd ../.. # Возвращаемся в корень

# --- Шаг 3: Завершение ---
echo "🎉 All tests passed!"
echo ""
echo "To see logs, run: docker-compose -p $PROJECT_NAME logs -f"
echo "To stop all services, run: docker-compose -p $PROJECT_NAME down"