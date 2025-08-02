package com.finsight.TransactionService.Entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "transactions")
@Schema(
        description = "Transaction entity",
        requiredProperties = {
                "account", "recipient", "description", "amount", "date", "classificationSource"
        }
)
public class Transaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transactions_id")
    @Schema(description = "Unique ID of the transaction", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long transactionsId;

    @NotBlank
    @Column(name = "account", nullable = false)
    @Schema(description = "Account number or identifier", example = "NL91ABNA0417164300")
    private String account;

    @Column(name = "category")
    @Schema(description = "Transaction category", example = "Groceries")
    private String category;

    @NotBlank
    @Column(name = "recipient", nullable = false)
    @Schema(description = "Recipient name", example = "Albert Heijn")
    private String recipient;

    @NotBlank
    @Column(name = "description", nullable = false)
    @Schema(description = "Transaction description", example = "Weekly grocery shopping")
    private String description;

    @NotNull
    @Column(name = "amount", nullable = false)
    @Schema(description = "Transaction amount in EUR (kan positief of negatief zijn)", example = "-52.75")
    private BigDecimal amount;

    @NotNull
    @Column(name = "date", nullable = false)
    @Schema(description = "Transaction date (ISO 8601 format)", example = "2025-08-02")
    private LocalDate date;

    @Column(name = "row_hash", unique = true)
    @Schema(description = "Unique hash to detect duplicates", example = "a12b34c56d78e90f")
    private String rowHash;


    // ENUM VAN MAKEN
    @NotNull
    @Column(name = "classification_source", nullable = false)
    @Schema(description = "Classification source ID", example = "0")
    private Integer classificationSource = 0;

    public Transaction(String account, String category, String recipient, String description, BigDecimal amount, LocalDate date) {
        this.account = account;
        this.category = category;
        this.recipient = recipient;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }
}

