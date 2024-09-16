package com.finsight.TransactionService.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iban_bban", nullable = false)
    private String ibanBban;

    @Column(name = "naam_tegenpartij", nullable = false)
    private String naamTegenpartij;

    @Column(name = "omschrijving", nullable = false)
    private String omschrijving;

    @Column(name = "bedrag", nullable = false)
    private BigDecimal bedrag;

    @Column(name = "datum", nullable = false)
    private LocalDate datum;
}

