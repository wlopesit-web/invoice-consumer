package com.portfolio.invoice_consumer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.invoice_consumer.dto.InvoiceDTO;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

    private final ObjectMapper objectMapper;

    @Value("${oci.tenant.namespace}")
    private String namespaceName;

    @Value("${oci.bucket.name}")
    private String bucketName;

    public ObjectStorageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void uploadInvoiceJson(InvoiceDTO dto) {
        String nomeArquivo = "nota_" + dto.getNumeroNota() + ".json";
        log.info("Iniciando a geracao do arquivo fisico e upload para o Object Storage OCI: {}", nomeArquivo);

        // O cliente ObjectStorageClient gerencia a conexao mTLS nativa com os servidores da Oracle Cloud
        try (ObjectStorageClient client = ObjectStorageClient.builder()
                .build(new ConfigFileAuthenticationDetailsProvider(ConfigFileReader.parseDefault()))) {

            // 1. Transforma o nosso DTO de volta em uma String JSON formatada e limpa
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
            byte[] jsonBytes = jsonContent.getBytes(StandardCharsets.UTF_8);

            // 2. Transforma os bytes na memoria em um fluxo de dados sem criar arquivos zumbis no WSL2
            try (InputStream inputStream = new ByteArrayInputStream(jsonBytes)) {

                // 3. Monta a requisicao oficial do SDK apontando para o seu Bucket na nuvem
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .namespaceName(namespaceName)
                        .bucketName(bucketName)
                        .objectName(nomeArquivo)
                        .contentType("application/json")
                        .contentLength((long) jsonBytes.length)
                        .putObjectBody(inputStream)
                        .build();

                log.info("Despachando fluxo de dados para a nuvem da Oracle...");
                PutObjectResponse response = client.putObject(putObjectRequest);

                log.info("Upload concluido com sucesso! Arquivo guardado no Object Storage. ETag: {}", response.getETag());
            }

        } catch (Exception e) {
            log.error("Erro critico de infraestrutura: Falha ao enviar o arquivo para o OCI Object Storage", e);
        }
    }
}
