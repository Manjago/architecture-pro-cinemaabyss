#!/bin/bash

# Script to run Postman tests using Newman against a Kubernetes cluster

# Function to display usage information
function show_usage {
  echo "Usage: $0"
  echo ""
  echo "This script runs the Kubernetes test suite in a Docker container."
  echo "It automatically detects the minikube IP and passes it to the container."
  echo ""
}

# Ensure we're in the right directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Create reports directory if it doesn't exist
mkdir -p reports

# --- NEW TEST EXECUTION BLOCK ---

echo "Running Kubernetes tests in Docker container..."

# Build the Docker image for tests
echo "Building test image..."
docker build -t cinemaabyss-api-tests . > /dev/null

# Get the minikube IP address
echo "Detecting Minikube IP..."
MINIKUBE_IP=$(minikube ip)
if [ -z "$MINIKUBE_IP" ]; then
  echo "❌ Error: Could not get minikube IP. Is minikube running?"
  exit 1
fi
echo "✅ Using minikube IP: $MINIKUBE_IP"

# Run the tests in a Docker container, adding the minikube host to its internal /etc/hosts
echo "🚀 Launching Newman tests..."
docker run --rm \
  --network=minikube \
  --add-host=cinemaabyss.example.com:$MINIKUBE_IP \
  -v "$(pwd)/reports:/app/reports" \
  cinemaabyss-api-tests --environment kubernetes

# Get the exit code
EXIT_CODE=$?

# Display results
if [ $EXIT_CODE -eq 0 ]; then
  echo "✅ All tests passed!"
else
  echo "❌ Some tests failed. Check the reports directory for details."
fi

exit $EXIT_CODE