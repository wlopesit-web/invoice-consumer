package com.portfolio.invoice_consumer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.invoice_consumer.dto.InvoiceDTO;
import com.portfolio.invoice_consumer.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvoiceListener {

    private static final Logger log = LoggerFactory.getLogger(InvoiceListener.class);

    private final ObjectMapper objectMapper;
    private final InvoiceService invoiceService;

    // O Spring Boot injeta automaticamente o conversor de JSON e a camada de negócio aqui
    public InvoiceListener(ObjectMapper objectMapper, InvoiceService invoiceService) {
        this.objectMapper = objectMapper;
        this.invoiceService = invoiceService;
    }

    // O método fica escutando a fila de faturamento usando a nossa fábrica customizada
    @KafkaListener(
            topics = "fila-faturamento",
            groupId = "faturamento-group"
    )
    public void listen(String mensagemJson) {
        log.info("==================================================");
        log.info("NOVA MENSAGEM CAPTURADA NA FILA DO KAFKA!");

        try {
           // Transforma o texto JSON recebido na nossa classe InvoiceDTO de forma limpa e moderna
           InvoiceDTO invoiceDTO = objectMapper.readValue(mensagemJson, InvoiceDTO.class);
           log.info("Nota Fiscal nº: {}", invoiceDTO.getNumeroNota());
           log.info("CNPJ Emitente: {}", invoiceDTO.getCnpjEmitente());
           log.info("Valor Total: R$ {}", invoiceDTO.getValorTotal());

            // DISPARO DO BANCO: Envia a nota mapeada para a camada de persistência do Oracle DB
            invoiceService.saveInvoice(invoiceDTO);

        } catch (Exception e) {
            log.error("Erro técnico sênior: Falha ao converter o JSON da Nota Fiscal", e);
        }
        log.info("==================================================");

        // TODO: Chamar o Service para salvar no Oracle DB e Object Storage
    }
}
