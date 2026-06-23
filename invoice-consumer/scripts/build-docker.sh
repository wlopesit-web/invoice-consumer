#!/bin/bash
# Força o script a parar imediatamente se qualquer comando falhar
set -e

echo "=== 🚀 STEP 1: INITIALIZING MAVEN BUILD COMPILATION ==="
chmod +x ./mvnw
./mvnw clean package -DskipTests

echo "=== 🐋 STEP 2: BUILDING PRODUCTION DOCKER IMAGE ==="
# O terminal do GitHub Actions vai injetar o seu usuário nas variáveis abaixo
docker build -t $DOCKER_USERNAME/invoice-consumer:latest .

echo "=== 📤 STEP 3: PUSHING DOCKER IMAGE TO THE REGISTRY ==="
docker push $DOCKER_USERNAME/invoice-consumer:latest

echo "=== ✅ IMAGE REPOSITORY UPDATED WITH SUCCESS ==="
