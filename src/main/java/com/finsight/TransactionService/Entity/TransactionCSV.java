package com.finsight.TransactionService.Entity;
import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCSV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment ID
    private Long id;

    @CsvBindByName(column = "IBAN/BBAN")
    private String account;

    @CsvBindByName(column = "Naam tegenpartij")
    private String recipient;

    @CsvBindByName(column = "Omschrijving-1")
    private String description;

    @CsvBindByName(column = "Bedrag")
    private String amount;

    @CsvBindByName(column = "Datum")
    private String date;

}

