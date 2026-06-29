#!/bin/bash
set -e

echo "=== ☁️ CONNECTING TO ORACLE CLOUD DISTRIBUTED CLUSTER REALM ==="

# 1. Valida se a tag do GitHub chegou até o script
if [ -z "$IMAGE_TAG" ]; then
    echo "❌ Erro: A variável IMAGE_TAG não foi definida!"
    exit 1
fi

# 2. Monta o caminho completo da nova imagem
FULL_IMAGE="wclcorp/invoice-consumer:${IMAGE_TAG}"

echo "🚀 Executing remote deployment for version: $IMAGE_TAG"

# 3. Atualiza a imagem e monitora o status do deploy dentro do mesmo bloco SSH
ssh -o StrictHostKeyChecking=no ubuntu@$ORACLE_CLOUD_IP << EOF
  echo "Changing container image..."
  kubectl set image deployment/invoice-consumer-deployment invoice-consumer-container=$FULL_IMAGE -n production

  echo "Monitoring deployment rollout status..."
  kubectl rollout status deployment/invoice-consumer-deployment -n production
EOF

echo "=== ✅ DEPLOY COMMAND EXECUTION COMPLETED ==="
