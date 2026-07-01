#!/bin/bash
echo "=== 🛑 STEP 1: CLEANING PREVIOUS CONTAINERS ==="
docker rm -f invoice-consumer-container 2>/dev/null

echo "=== 🚀 STEP 2: DISPARANDO GO-LIVE COM COFRE DE CREDENCIAIS ==="

#CHAVES VAULT DA OCI
OCID_USER="ocid1.vaultsecret.oc1.sa-saopaulo-1.amaaaaaak2fyzeaalg3tzg5zjgegru67z545y5p3qvk3ur2c4xjcaa6wegdq"
OCID_PASS="ocid1.vaultsecret.oc1.sa-saopaulo-1.amaaaaaak2fyzeaa6p7wl7tv7cdvjo63sddkfzn2ptlfkzo5g6mx22lleara"
OCID_WLLT="ocid1.vaultsecret.oc1.sa-saopaulo-1.amaaaaaak2fyzeaacl4mqwsv73k66ftctlvy4pwj5mkcz7an2iohejmeivqq"

echo "Puxando Usuário do Banco..."
#VAL_USER=$(oci vault secret-bundle get --secret-id $OCID_USER --query "data.\"secret-content\".content" --raw-output | base64 --decode)
#VAL_USER=$(oci secrets secret-bundle get --secret-id $OCID_USER --query "data.\"secret-content\".content" --raw-output | base64 --decode)
VAL_USER=$(oci secrets secret-bundle get --secret-id $OCID_USER --query 'data."secret-bundle-content".content' --raw-output | base64 --decode)
echo "Puxando Senha do Banco..."
#VAL_PASS=$(oci vault secret-bundle get --secret-id $OCID_PASS --query "data.\"secret-content\".content" --raw-output | base64 --decode)
#VAL_PASS=$(oci secrets secret-bundle get --secret-id $OCID_USER --query "data.\"secret-content\".content" --raw-output | base64 --decode)
VAL_PASS=$(oci secrets secret-bundle get --secret-id $OCID_PASS --query 'data."secret-bundle-content".content' --raw-output | base64 --decode)
echo "Puxando Senha da Wallet..."
#VAL_WLLT=$(oci vault secret-bundle get --secret-id $OCID_WLLT --query "data.\"secret-content\".content" --raw-output | base64 --decode)
#VAL_WLLT=$(oci secrets secret-bundle get --secret-id $OCID_USER --query "data.\"secret-content\".content" --raw-output | base64 --decode)
VAL_WLLT=$(oci secrets secret-bundle get --secret-id $OCID_WLLT --query 'data."secret-bundle-content".content' --raw-output | base64 --decode)

docker run -d \
  --name invoice-consumer-container \
  --restart always \
  --network kafka-shared-network \
  -p 8082:8082 \
  -e SERVER_PORT=8082 \
  -v /home/opc/oracle_wallet:/app/oracle_wallet \
  -v /home/opc/.oci:/root/.oci:ro \
  -v /home/opc/.oci:/home/opc/.oci:ro \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-portfolio:29092 \
  -e DB_USER="$VAL_USER" \
  -e DB_PASSWORD="$VAL_PASS" \
  -e WALLET_PASSWORD="$VAL_WLLT" \
  -e TNS_ADMIN="/app/oracle_wallet" \
  -e DB_ORACLE_URL="jdbc:oracle:thin:@wclp_high" \
  -e SPRING_DATASOURCE_USERNAME="$VAL_USER" \
  -e SPRING_DATASOURCE_PASSWORD="$VAL_PASS" \
  -e JAVA_OPTS="-Xmx256M -Xms128M" \
  invoice-consumer-app

echo "====================================================="
echo "🟢 ECOSSISTEMA INICIADO COM ACESSO AO ORACLE AUTONOMOUS!"
echo "====================================================="
