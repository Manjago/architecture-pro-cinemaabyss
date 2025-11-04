#!/bin/bash
echo "Testing proxy on localhost:8000..."
echo ""
echo "=== Test 1: /get ==="
curl http://localhost:8000/get
echo ""
echo ""
echo "=== Test 2: /status/200 ==="
curl http://localhost:8000/status/200
echo ""
echo ""
echo "=== Test 3: /json ==="
curl http://localhost:8000/json
echo ""