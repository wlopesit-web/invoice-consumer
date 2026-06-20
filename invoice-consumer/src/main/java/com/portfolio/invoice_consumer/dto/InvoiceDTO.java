package com.portfolio.invoice_consumer.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class InvoiceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String numeroNota;
    private String cnpjEmitente;
    private String cnpjDestinatario;
    private BigDecimal valorTotal;
    private String itens;

    // Construtores
    public InvoiceDTO() {}

    public InvoiceDTO(String numeroNota, String cnpjEmitente, String cnpjDestinatario, BigDecimal valorTotal, String itens) {
        this.numeroNota = numeroNota;
        this.cnpjEmitente = cnpjEmitente;
        this.cnpjDestinatario = cnpjDestinatario;
        this.valorTotal = valorTotal;
        this.itens = itens;
    }

    // Getters e Setters
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
}
