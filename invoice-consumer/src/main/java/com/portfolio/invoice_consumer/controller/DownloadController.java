package com.portfolio.invoice_consumer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/downloads")
@CrossOrigin(origins = "*")
public class DownloadController {

    private static final Logger log = LoggerFactory.getLogger(DownloadController.class);

    @Value("${oci.tenant.namespace}")
    private String namespaceName;

    @Value("${oci.bucket.name}")
    private String bucketName;

    @GetMapping("/url/{numeroNota}")
    public ResponseEntity<?> getSecureDownloadUrl(@PathVariable String numeroNota) {
        String nomeArquivo = "nota_" + numeroNota + ".json";
        log.info("Solicitacao de link seguro recebida para o arquivo: {}", nomeArquivo);

        // 1. Carrega o arquivo de configuração padrão e instancia o provedor (versao 3.x)
        try {
            com.oracle.bmc.ConfigFileReader.ConfigFile configFile = com.oracle.bmc.ConfigFileReader.parseDefault();
            com.oracle.bmc.auth.AuthenticationDetailsProvider provider = new com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider(configFile);

            try (com.oracle.bmc.objectstorage.ObjectStorageClient client = com.oracle.bmc.objectstorage.ObjectStorageClient.builder()
                    .build(provider)) {


            // Configura o link seguro para expirar em 30 minutos
            Date dataExpiracao = new Date(System.currentTimeMillis() + (30 * 60 * 1000));

            // 2. Detalhes do PAR (Atenção ao 'a' minúsculo em Preauthenticated)
            com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails details =
                    com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.builder()
                            .name("PAR_DOWNLOAD_" + numeroNota)
                            .accessType(com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                            .objectName(nomeArquivo)
                            .timeExpires(dataExpiracao)
                            .build();

            // 3. Montagem da Requisição (Atenção ao 'a' minúsculo em Preauthenticated)
            com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest request =
                    com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest.builder()
                            .namespaceName(namespaceName)
                            .bucketName(bucketName)
                            .createPreauthenticatedRequestDetails(details)
                            .build();

            log.info("Gerando credencial PAR temporaria na nuvem da Oracle...");

            // 4. Executa a chamada e captura a Resposta
            com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse response =
                    client.createPreauthenticatedRequest(request);

            // Monta a URL fisica final de download baseada no endpoint da OCI
            String urlFinal = "https://objectstorage.sa-saopaulo-1.oraclecloud.com"
                    + response.getPreauthenticatedRequest().getAccessUri();

            log.info("Link seguro gerado com sucesso!");
            return ResponseEntity.ok(Map.of("downloadUrl", urlFinal));
          } // FECHA O SEGUNDO TRY AUTOMATICAMENTE AQU

        } catch (Exception e) {
            log.error("Erro ao gerar credencial de download na nuvem", e);
            return ResponseEntity.status(500).body("Erro de infraestrutura ao buscar o arquivo na nuvem.");
        }
    }

}
