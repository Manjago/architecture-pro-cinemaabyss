#!/bin/bash
echo "Stopping httpbin containers..."
docker stop httpbin-monolith httpbin-movies 2>/dev/null || true
docker rm httpbin-monolith httpbin-movies 2>/dev/null || true
echo "✅ Stopped"