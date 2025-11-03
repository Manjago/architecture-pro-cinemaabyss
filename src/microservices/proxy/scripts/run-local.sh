#!/bin/bash
export PORT=8000
export MONOLITH_URL=http://localhost:8080
export MOVIES_SERVICE_URL=http://localhost:8081
export GRADUAL_MIGRATION=true
export MOVIES_MIGRATION_PERCENT=50

cd "$(dirname "$0")/.."
./mvnw clean compile exec:java -Dexec.mainClass="com.cinemaabyss.proxy.Main"
