#!/bin/bash

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

cd "$(dirname "$0")/.."

case "$1" in
  start)
    echo -e "${YELLOW}Starting local infrastructure (Kafka, Zookeeper, PostgreSQL)...${NC}"
    docker-compose -f docker-compose.local.yml up -d
    echo -e "${GREEN}Infrastructure started!${NC}"
    echo ""
    echo "Services available at:"
    echo "  - Kafka: localhost:9092"
    echo "  - PostgreSQL: localhost:5432"
    ;;
  stop)
    echo -e "${YELLOW}Stopping local infrastructure...${NC}"
    docker-compose -f docker-compose.local.yml down
    echo -e "${GREEN}Infrastructure stopped!${NC}"
    ;;
  restart)
    $0 stop
    sleep 2
    $0 start
    ;;
  logs)
    docker-compose -f docker-compose.local.yml logs -f
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|logs}"
    exit 1
    ;;
esac