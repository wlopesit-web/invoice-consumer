package com.portfolio.invoice_consumer.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_INVOICES")
public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_nota", nullable = false, length = 50)
    private String numeroNota;

    @Column(name = "cnpj_emitente", nullable = false, length = 14)
    private String cnpjEmitente;

    @Column(name = "cnpj_destinatario", nullable = false, length = 14)
    private String cnpjDestinatario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Lob // Define como tipo CLOB/TEXT no banco para aceitar listas grandes de strings
    @Column(name = "itens", columnDefinition = "CLOB")
    private String itens;

    @Column(name = "data_processamento", nullable = false)
    private LocalDateTime dataProcessamento;

    // Construtores obrigatórios do JPA
    public Invoice() {}

    public Invoice(String numeroNota, String cnpjEmitente, String cnpjDestinatario, BigDecimal valorTotal, String itens) {
        this.numeroNota = numeroNota;
        this.cnpjEmitente = cnpjEmitente;
        this.cnpjDestinatario = cnpjDestinatario;
        this.valorTotal = valorTotal;
        this.itens = itens;
        this.dataProcessamento = LocalDateTime.now(); // Garante o timestamp automático da gravação
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroNota() { return numeroNota; }
    public void setNumeroNota(String numeroNota) { this.numeroNota = numeroNota; }

    public String getCnpjEmitente() { return cnpjEmitente; }
    public void setCnpjEmitente(String cnpjEmitente) { this.cnpjEmitente = cnpjEmitente; }

    public String getCnpjDestinatario() { return cnpjDestinatario; }
    public void setCnpjDestinatario(String cnpjDestinatario) { this.cnpjDestinatario = cnpjDestinatario; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public String getItens() { return itens; }
    public void setItens(String itens) { this.itens = itens; }

    public LocalDateTime getDataProcessamento() { return dataProcessamento; }
    public void setDataProcessamento(LocalDateTime dataProcessamento) { this.dataProcessamento = dataProcessamento; }
}
