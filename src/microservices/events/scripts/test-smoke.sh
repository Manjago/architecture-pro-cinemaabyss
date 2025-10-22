#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Running smoke tests for Events Service..."

# Проверяем, что сервис запущен
if ! curl -s http://localhost:8082/api/events/health > /dev/null 2>&1; then
    echo -e "${RED}❌ Service is not running on port 8082${NC}"
    exit 1
fi

# Тест 1: Health check
echo -n "Testing health check endpoint... "
RESPONSE=$(curl -s http://localhost:8082/api/events/health)
if echo "$RESPONSE" | grep -q '"status":true'; then
    echo -e "${GREEN}✓ PASSED${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    echo "Expected: {\"status\":true}"
    echo "Got: $RESPONSE"
    exit 1
fi

echo -e "\n${GREEN}All smoke tests passed!${NC}"