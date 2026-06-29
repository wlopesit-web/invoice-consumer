package com.portfolio.invoice_consumer.controller;

import com.portfolio.invoice_consumer.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history") // Rota exclusiva e limpa para o histórico!
@CrossOrigin(origins = "*")
public class InvoiceHistoryController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceHistoryController.class);

    @Autowired
    private InvoiceService invoiceService; // Injeta apenas a camada de serviço

    @GetMapping("/all")
    public ResponseEntity<?> getAllInvoicesFromDatabase() {
        log.info("API de Historico acionada pelo Front-end.");
        try {
            List<?> historicoReal = invoiceService.getInvoiceHistoryFromDatabase();
            log.info("Historico enviado para a tela. Total de registros: {}", historicoReal.size());
            return ResponseEntity.ok(historicoReal);
        } catch (Exception e) {
            log.error("Erro critico na API de historico", e);
            return ResponseEntity.status(500).body("Erro de infraestrutura ao ler histórico.");
        }
    }

    @DeleteMapping("/delete/{numeroNota}")
    public ResponseEntity<?> deleteInvoice(@PathVariable String numeroNota) {
        log.warn("Solicitacao de exclusao recebida para a nota nº: {}", numeroNota);
        try {
            // O Controller repassa a ordem de destruição para a camada Service
            invoiceService.deleteInvoiceFromSystem(numeroNota);

            log.info("Nota nº {} excluida com sucesso do ecossistema híbrido.", numeroNota);
            return ResponseEntity.ok(java.util.Map.of("message", "Invoice successfully deleted from database and OCI storage."));
        } catch (Exception e) {
            log.error("Falha critica ao executar a exclusao da nota " + numeroNota, e);
            return ResponseEntity.status(500).body("Internal error during cloud/database resource deletion.");
        }
    }
}
