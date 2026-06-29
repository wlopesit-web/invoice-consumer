#!/bin/bash
# Força o script a parar imediatamente se qualquer comando falhar
set -e
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sem Cor

echo -e "${YELLOW}=== 🔍 VALIDATING INITIAL CONFIGURATIONS ===${NC}"

# 1. Validação da variável do Docker Username
if [ -z "$DOCKER_USERNAME" ]; then
     echo -e "${RED}❌ ERROR: The \$DOCKER_USERNAME variable is empty or not defined!${NC}"
     echo -e "${YELLOW}👉 To run locally, execute first: export DOCKER_USERNAME='your_username'${NC}"
     exit 1
fi

# 2. Docker Username validation
IMAGE_NAME="invoice-consumer"
REPO_NAME="wclcorp"
#FULL_IMAGE_TAG="$DOCKER_USERNAME/$IMAGE_NAME:latest"
FULL_IMAGE_TAG="${DOCKER_USERNAME:-$REPO_NAME}/$IMAGE_NAME:${IMAGE_TAG:-latest}"

echo -e "${GREEN}✓ Docker username detected: $DOCKER_USERNAME${NC}"
echo -e "${GREEN}✓ Target image tag: $FULL_IMAGE_TAG${NC}\n"

echo -e "${YELLOW}=== 🚀 STEP 1: INITIALIZING MAVEN BUILD COMPILATION ===${NC}"

if [ ! -f "./mvnw" ]; then
    echo -e "${RED}❌ ERROR: './mvnw' file not found in the project root folder!${NC}"
    exit 1
fi

echo "Granting execution permission to Maven Wrapper..."
chmod +x ./mvnw
echo "Running clean package..."
./mvnw clean package -DskipTests 2>&1 | tee maven-build.log

# Captures the exit status of the Maven command
if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo -e "${RED}❌ FATAL ERROR: Maven compilation failed! Check logs at maven-build.log${NC}"
    exit 1
fi

echo -e "${GREEN}===========================================${NC}"
echo -e "${GREEN}✅ MAVEN COMPILATION FINISHED WITH SUCCESS!${NC}\n"
echo -e "${GREEN}===========================================${NC}"

echo -e "${YELLOW}=== 🐳 STEP 2: BUILDING PRODUCTION DOCKER IMAGE ===${NC}"

if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}❌ ERROR: 'Dockerfile' file not found in this folder!${NC}"
    exit 1
fi

# O terminal do GitHub Actions vai injetar o seu usuário nas variáveis abaixo
docker build -t "$FULL_IMAGE_TAG" . 2>&1 | tee docker-build.log

# --- Verifica se o robô do Docker concluiu o Build com sucesso ---
if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo -e "${RED}===================================================${NC}"
    echo -e "${RED}🚨 ERROR DETECTED DURING COMPILED BUILD! 🚨${NC}"
    echo -e "${RED}===================================================${NC}"
    echo -e "👉 Inspecione os erros acima ou abra o arquivo: scripts/build.log"
    exit 1
fi

echo -e "${GREEN}==================================================${NC}"
echo -e "${GREEN}🏆 DOCKER IMAGE COMPILED WITH SUCCESS ABSOLUTE!🏆${NC}"
echo -e "${GREEN}==================================================${NC}"
echo -e "💡 Arquivo de auditoria salvo com sucesso em: scripts/build.log"

echo -e "${YELLOW}=== 📤 STEP 3: PUSHING DOCKER IMAGE TO THE REGISTRY ===${NC}"

#No log for push for secure
echo "Pushing image $FULL_IMAGE_TAG to Docker Hub..."
docker push "$FULL_IMAGE_TAG"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ FATAL ERROR: Failed to push the image to Docker Hub!${NC}"
    echo -e "${YELLOW}👉 Make sure you ran 'docker login' on this machine before running the script.${NC}"
    exit 1
fi

echo -e "${GREEN}=================================================${NC}"
echo -e "${GREEN}=== ✅ IMAGE REPOSITORY UPDATED WITH SUCCESS ===${NC}"
echo -e "${GREEN}=================================================${NC}"





