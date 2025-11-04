#!/bin/bash

echo "=== Testing all HTTP methods with proxy ==="
echo ""

echo "📗 GET /api/movies"
curl -s http://localhost:8000/api/movies | jq -r '.source, .method' | head -2
echo ""

echo "📘 POST /api/movies"
curl -s -X POST http://localhost:8000/api/movies \
  -H "Content-Type: application/json" \
  -d '{"title": "New Movie"}' | jq -r '.source, .method, .message' | head -3
echo ""

echo "📙 PUT /api/movies/1"
curl -s -X PUT http://localhost:8000/api/movies/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated Movie"}' | jq -r '.source, .method, .message' | head -3
echo ""

echo "📕 DELETE /api/movies/1"
curl -s -X DELETE http://localhost:8000/api/movies/1 | jq -r '.source, .method, .message' | head -3
echo ""

echo "📔 PATCH /api/movies/1"
curl -s -X PATCH http://localhost:8000/api/movies/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Patched"}' | jq -r '.source, .method, .message' | head -3
echo ""

echo "🔵 GET /test (non-movies path)"
curl -s http://localhost:8000/test
echo ""
