#!/bin/bash
echo "Starting httpbin in Docker on ports 8080 and 8081..."

# Останавливаем старые контейнеры, если есть
docker stop httpbin-monolith httpbin-movies 2>/dev/null || true
docker rm httpbin-monolith httpbin-movies 2>/dev/null || true

# Запускаем два httpbin (имитируем монолит и микросервис movies)
docker run -d --name httpbin-monolith -p 8080:80 kennethreitz/httpbin
docker run -d --name httpbin-movies -p 8081:80 kennethreitz/httpbin

echo ""
echo "✅ httpbin-monolith running on http://localhost:8080"
echo "✅ httpbin-movies running on http://localhost:8081"
echo ""
echo "Test with: curl http://localhost:8080/json"