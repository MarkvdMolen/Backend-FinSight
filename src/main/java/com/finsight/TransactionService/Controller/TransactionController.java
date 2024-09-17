package com.finsight.TransactionService.Controller;
import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.Entity.TransactionCSV;
import com.finsight.TransactionService.Service.TransactionService;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Endpoint om alle transacties op te halen
    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    // PUT endpoint om een transactie te updaten op basis van het ID
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @RequestBody Transaction updatedTransaction) {

        Optional<Transaction> existingTransactionOpt = transactionService.getTransactionById(id);

        if (existingTransactionOpt.isPresent()) {
            Transaction existingTransaction = existingTransactionOpt.get();

            // Update de velden van de bestaande transactie met de nieuwe waarden
            existingTransaction.setAccount(updatedTransaction.getAccount());
            existingTransaction.setCategory(updatedTransaction.getCategory());
            existingTransaction.setRecipient(updatedTransaction.getRecipient());
            existingTransaction.setDescription(updatedTransaction.getDescription());
            existingTransaction.setAmount(updatedTransaction.getAmount());
            existingTransaction.setDate(updatedTransaction.getDate());

            // Sla de bijgewerkte transactie op
            transactionService.saveTransaction(existingTransaction);
            return ResponseEntity.ok(existingTransaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a file");
        }

        try {
            // Maak een CSVReader
            CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            // Gebruik een strategie op basis van de kolomkoppen
            HeaderColumnNameMappingStrategy<TransactionCSV> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(TransactionCSV.class);

            // Verwerk de CSV-bestanden en map de gegevens naar het TransactionCsvModel
            List<TransactionCSV> csvRecords = new CsvToBeanBuilder<TransactionCSV>(reader)
                    .withMappingStrategy(strategy)
                    .withSkipLines(1) // Sla de kopregel over
                    .build()
                    .parse();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Pas dit aan als het CSV-bestand een ander datumformaat gebruikt

            for (TransactionCSV csvRecord : csvRecords) {
                LocalDate parsedDate = LocalDate.parse(csvRecord.getDate(), formatter);  // Parse de datum met de formatter

                // Oplossing voor het getal met komma's
                BigDecimal parsedAmount = parseAmount(csvRecord.getAmount());

                transactionService.saveTransaction(new Transaction(
                        csvRecord.getAccount(),
                        null,
                        csvRecord.getRecipient(),
                        csvRecord.getDescription(),
                        parsedAmount,
                        parsedDate));  // Gebruik de geparsete datum
            }

            return ResponseEntity.ok("File uploaded and processed successfully!");

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + ex.getMessage());
        }
    }

    private BigDecimal parseAmount(String amountStr) {
        // Vervang komma door punt voor de BigDecimal conversie
        String normalizedAmount = amountStr.replace(",", ".");
        return new BigDecimal(normalizedAmount);
    }
}