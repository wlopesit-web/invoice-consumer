#!/bin/bash
# Força o script a parar imediatamente se qualquer comando falhar
set -e
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sem Cor

echo -e "${YELLOW}=== 🔍 VALIDATING INITIAL CONFIGURATIONS ===${NC}"

# 1. Validação da variável do Docker Username
#if [ -z "$DOCKER_USERNAME" ]; then
#     echo -e "${RED}❌ ERROR: The \$DOCKER_USERNAME variable is empty or not defined!${NC}"
#     echo -e "${YELLOW}👉 To run locally, execute first: export DOCKER_USERNAME='your_username'${NC}"
#     exit 1
#fi

echo -e "${YELLOW}=== 🚀 STEP 1: INITIALIZING MAVEN TESTS ===${NC}"

if [ ! -f "./mvnw" ]; then
    echo -e "${RED}❌ ERROR: './mvnw' file not found in the project root folder!${NC}"
    exit 1
fi

echo "Granting execution permission to Maven Wrapper..."
chmod +x ./mvnw
echo "Running clean test..."
./mvnw clean test 2>&1 | tee maven-test.log

# Captures the exit status of the Maven command
if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo -e "${RED}❌ FATAL ERROR: Maven test failed! Check logs at maven-test.log${NC}"
    exit 1
fi

echo -e "${GREEN}===========================================${NC}"
echo -e "${GREEN}✅ MAVEN TEST FINISHED WITH SUCCESS!${NC}\n"
echo -e "${GREEN}===========================================${NC}"




