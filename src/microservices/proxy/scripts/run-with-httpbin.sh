#!/bin/bash
export PORT=8000
export MONOLITH_URL=https://httpbin.org
export MOVIES_SERVICE_URL=https://httpbin.org
export GRADUAL_MIGRATION=true
export MOVIES_MIGRATION_PERCENT=50

cd "$(dirname "$0")/.."
./mvnw clean compile exec:java -Dexec.mainClass="com.cinemaabyss.proxy.Main"