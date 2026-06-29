package com.portfolio.invoice_consumer.service;

import com.portfolio.invoice_consumer.domain.Invoice;
import com.portfolio.invoice_consumer.dto.InvoiceDTO;
import com.portfolio.invoice_consumer.repository.InvoiceRepository;
import com.portfolio.invoice_consumer.storage.ObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    // Injeção do repositório automático do Spring Data JPA
    private final InvoiceRepository invoiceRepository;
    private final ObjectStorageService objectStorageService;

    @Value("${oci.tenant.namespace}")
    private String namespaceName;

    @Value("${oci.bucket.name}")
    private String bucketName;

    /*
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }
    */

    // injeta automaticamente o repositório e o serviço de upload via construtor
    public InvoiceService(InvoiceRepository invoiceRepository, ObjectStorageService objectStorageService) {
        this.invoiceRepository = invoiceRepository;
        this.objectStorageService = objectStorageService;
    }


    // Método que converte o DTO em Entidade e salva no Oracle DB
    @Transactional // Garante a segurança da transação (ACID) no banco de dados
    public void saveInvoice(InvoiceDTO dto) {
        log.info("Iniciando a persistência da Nota Fiscal nº {} no Oracle DB", dto.getNumeroNota());

        // Transforma os dados que vieram do contrato do Kafka para a nossa entidade de banco de dados
        Invoice invoice = new Invoice(
                dto.getNumeroNota(),
                dto.getCnpjEmitente(),
                dto.getCnpjDestinatario(),
                dto.getValorTotal(),
                dto.getItens()
        );

        // O Hibernate entra em ação aqui e executa o INSERT INTO TB_INVOICES de forma oculta
        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info("Nota Fiscal salva com sucesso no banco de dados! ID gerado: {}", savedInvoice.getId());

        try {
            //Gera o arquivo JSON físico e manda pro o Bucket da nuvem
            objectStorageService.uploadInvoiceJson(dto);
        } catch (Exception e) {
            log.error("Erro secundario: Dados salvos no banco, mas falhou ao enviar para o Object Storage", e);
        }


    }

    public java.util.List<?> getInvoiceHistoryFromDatabase() {
        log.info("Executing database query via repository to fetch all invoices...");
        return invoiceRepository.findAll(); // Executa o SELECT * FROM real no banco de dados via JPA
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteInvoiceFromSystem(String numeroNota) throws Exception {
        String nomeArquivo = "nota_" + numeroNota + ".json";
        log.warn("Initializing cascading deletion for invoice nº: {}", numeroNota);

        // 1. DELETA DO BANCO DE DADOS RELACIONAL VIA JPA
        // Busca a nota e remove usando o seu repositório (ajuste o metodo se o seu repositório usar outra nomenclatura)
        Long idNumerico = Long.parseLong(numeroNota);
        invoiceRepository.deleteDirectlyByNumeroNota(numeroNota);
        log.info("Record successfully purged from relational database.");

        // 2. DELETA O ARQUIVO FÍSICO DO BUCKET NA ORACLE CLOUD (OCI)
        try {
            com.oracle.bmc.ConfigFileReader.ConfigFile configFile = com.oracle.bmc.ConfigFileReader.parseDefault();
            com.oracle.bmc.auth.AuthenticationDetailsProvider provider = new com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider(configFile);

            try (com.oracle.bmc.objectstorage.ObjectStorageClient client = com.oracle.bmc.objectstorage.ObjectStorageClient.builder().build(provider)) {

                com.oracle.bmc.objectstorage.requests.DeleteObjectRequest request =
                        com.oracle.bmc.objectstorage.requests.DeleteObjectRequest.builder()
                                .namespaceName(this.namespaceName)
                                .bucketName(this.bucketName)
                                .objectName(nomeArquivo)
                                .build();

                log.info("Sending authenticated DeleteObject token to sa-saopaulo-1 datacenter...");
                client.deleteObject(request);
                log.info("Physical file successfully obliterated from OCI Object Storage.");
            }
        } catch (com.oracle.bmc.model.BmcException e) {
            // Se der erro 404 (arquivo não encontrado na nuvem), apenas loga um aviso e NÃO trava o sistema!
            if (e.getStatusCode() == 404) {

                log.warn("Cloud object {} was already missing from OCI bucket. Proceeding layout cleaning.", nomeArquivo);
            } else {
                // Se for qualquer outro erro de rede real (ex: falta de internet), aí sim repassa o erro
                throw e;
            }        }
    }

}
