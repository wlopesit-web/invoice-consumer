#!/bin/bash
# scripts/deploy-docker.sh
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}=== 🔍 INITIALIZING REMOTE DEPLOYMENT ON OCI VM ===${NC}"

if [ -z "$IMAGE_TAG" ]; then
  echo -e "${RED}❌ ERROR: The \$IMAGE_TAG variable is empty or not defined!${NC}"
  exit 1
fi

if [ -z "$DOCKER_USERNAME" ]; then
     echo -e "${RED}❌ ERROR: The \$DOCKER_USERNAME variable is empty or not defined!${NC}"
     echo -e "${YELLOW}👉 To run locally, execute first: export DOCKER_USERNAME='your_username'${NC}"
     exit 1
fi

IMAGE_NAME="invoice-consumer"
FULL_IMAGE="${DOCKER_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG:-latest}"

echo -e "${YELLOW}=== 🐳 STEP 1: PULLING LATEST IMAGE ON THE VM ===${NC}"
docker pull "$FULL_IMAGE"

echo -e "${YELLOW}=== 🚀 STEP 2: RESTARTING THE APPLICATION ===${NC}"
# Verifies where the start-app.sh is located and executes it passing the unique tag as parameter
if [ -f "/home/opc/projetos/invoice-consumer/invoice-consumer/scripts/start-app.sh" ]; then
  echo "Triggering local start-app.sh script with tag: $IMAGE_TAG"
  bash /home/opc/projetos/invoice-consumer/invoice-consumer/scripts/start-app.sh "$IMAGE_TAG"

  if [ $? -ne 0  ]; then
      echo -e "${RED}❌ FATAL ERROR: Triggering local start-app.sh script${NC}"
      exit 1
  fi
else
  echo -e "${RED}❌ FATAL ERROR: 'start-app.sh' was not found in home directory!${NC}"
  exit 1
fi

echo -e "${GREEN}=================================================${NC}"
echo -e "${GREEN}=== ✅ DEPLOYMENT FINISHED WITH SUCCESS ON OCI ===${NC}"
echo -e "${GREEN}=================================================${NC}"
