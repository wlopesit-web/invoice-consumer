package com.portfolio.invoice_consumer.repository;

import com.portfolio.invoice_consumer.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Herdando o JpaRepository, o Spring já cria os métodos save(), findById(), delete() automaticamente!
    @Modifying
    @Query("DELETE FROM Invoice i WHERE i.numeroNota = :numeroNota")
    void deleteDirectlyByNumeroNota(@Param("numeroNota") String numeroNota);
}
