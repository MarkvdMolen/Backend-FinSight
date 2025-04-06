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
    private Long transactions_id;

    @Column(name = "account", nullable = false)
    private String account;

    @Column(name = "category", nullable = true)
    private String category;

    @Column(name = "recipient ", nullable = false)
    private String recipient;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "row_hash", unique = true)
    private String rowHash;

    public Transaction(String account, String category, String recipient, String description, BigDecimal amount, LocalDate date) {
        this.account = account;
        this.category = category;
        this.recipient = recipient;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }
}

