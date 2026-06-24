#!/bin/bash
echo "=== 🛑 STEP 1: CLEANING PREVIOUS CONTAINERS ==="
docker rm -f invoice-consumer-container 2>/dev/null

echo "=== 🚀 STEP 2: DISPARANDO GO-LIVE DO SPRING BOOT NA NUVEM ==="
docker run -d \
  --name invoice-consumer-container \
  --restart always \
  -p 8081:8081 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=147.15.123.230:9092 \
  -e JAVA_OPTS="-Xmx256M -Xms128M" \
  invoice-consumer-app

echo "====================================================="
echo "🟢 MICROSSERVIÇO UP E CONECTADO NO KAFKA DA OCI!"
echo "====================================================="
