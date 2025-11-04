#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
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

# Тест 2: User Event
echo -n "Testing POST /api/events/user... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8082/api/events/user \
    -H "Content-Type: application/json" \
    -d '{"user_id":123,"username":"testuser","email":"test@example.com","action":"registered","timestamp":"2023-01-15T14:30:00Z"}')

RESPONSE=$(curl -s -X POST http://localhost:8082/api/events/user \
    -H "Content-Type: application/json" \
    -d '{"user_id":123,"username":"testuser","email":"test@example.com","action":"registered","timestamp":"2023-01-15T14:30:00Z"}')

if [ "$HTTP_CODE" = "201" ] && echo "$RESPONSE" | grep -q '"status":"success"' && echo "$RESPONSE" | grep -q '"type":"user"'; then
    echo -e "${GREEN}✓ PASSED${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    echo "Expected: HTTP 201 with status=success and type=user"
    echo "Got HTTP $HTTP_CODE: $RESPONSE"
    exit 1
fi

# Тест 3: Movie Event
echo -n "Testing POST /api/events/movie... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8082/api/events/movie \
    -H "Content-Type: application/json" \
    -d '{"movie_id":42,"title":"Inception","action":"viewed","user_id":123,"rating":8.8}')

RESPONSE=$(curl -s -X POST http://localhost:8082/api/events/movie \
    -H "Content-Type: application/json" \
    -d '{"movie_id":42,"title":"Inception","action":"viewed","user_id":123,"rating":8.8}')

if [ "$HTTP_CODE" = "201" ] && echo "$RESPONSE" | grep -q '"status":"success"' && echo "$RESPONSE" | grep -q '"type":"movie"'; then
    echo -e "${GREEN}✓ PASSED${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    echo "Expected: HTTP 201 with status=success and type=movie"
    echo "Got HTTP $HTTP_CODE: $RESPONSE"
    exit 1
fi

# Тест 4: Payment Event
echo -n "Testing POST /api/events/payment... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8082/api/events/payment \
    -H "Content-Type: application/json" \
    -d '{"payment_id":789,"user_id":123,"amount":9.99,"status":"completed","timestamp":"2023-01-15T14:30:00Z","method_type":"credit_card"}')

RESPONSE=$(curl -s -X POST http://localhost:8082/api/events/payment \
    -H "Content-Type: application/json" \
    -d '{"payment_id":789,"user_id":123,"amount":9.99,"status":"completed","timestamp":"2023-01-15T14:30:00Z","method_type":"credit_card"}')

if [ "$HTTP_CODE" = "201" ] && echo "$RESPONSE" | grep -q '"status":"success"' && echo "$RESPONSE" | grep -q '"type":"payment"'; then
    echo -e "${GREEN}✓ PASSED${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    echo "Expected: HTTP 201 with status=success and type=payment"
    echo "Got HTTP $HTTP_CODE: $RESPONSE"
    exit 1
fi

echo -e "\n${GREEN}All smoke tests passed!${NC}"
echo -e "${YELLOW}Summary: 4 tests passed (health, user, movie, payment)${NC}"