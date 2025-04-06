package com.finsight.TransactionService.Controller;
import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.Entity.TransactionCSV;
import com.finsight.TransactionService.Service.TransactionService;
import com.finsight.UtilClasses.HashUtil;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Retrieve paginated, sorted, and filtered transactions.
     * @param sortBy The field to sort by.
     * @param direction The sort direction (asc or desc).
     * @param filterCriteria A string to filter the transactions (e.g., by recipient or description).
     * @param page The current page of results.
     * @param size The number of results per page.
     * @return Paginated and filtered transactions.
     */
    @GetMapping
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(value = "sort", defaultValue = "date") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam(value = "filter", defaultValue = "") String filterCriteria,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        // Create Sort object based on direction
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // Create Pageable object for pagination and sorting
        Pageable pageable = PageRequest.of(page, size, sort);

        // Call the service to find transactions with filtering, sorting, and pagination
        Page<Transaction> transactions = transactionService.findTransactions(filterCriteria, pageable);

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
                LocalDate parsedDate = LocalDate.parse(csvRecord.getDate(), formatter);
                BigDecimal parsedAmount = parseAmount(csvRecord.getAmount());

                String hash = HashUtil.generateTransactionHash(
                        csvRecord.getAccount(),
                        csvRecord.getRecipient(),
                        csvRecord.getDescription(),
                        parsedAmount,
                        parsedDate
                );

                // Check of transactie met deze hash al bestaat
                boolean exists = transactionService.existsByRowHash(hash);

                if (!exists) {
                    Transaction transaction = Transaction.builder()
                            .account(csvRecord.getAccount())
                            .recipient(csvRecord.getRecipient())
                            .description(csvRecord.getDescription())
                            .amount(parsedAmount)
                            .date(parsedDate)
                            .rowHash(hash)
                            .build();

                    transactionService.saveTransaction(transaction);
                }
            }

            return ResponseEntity.ok("File uploaded and processed successfully!");

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Given file is of wrong format");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + ex.getMessage());
        }
    }

    private BigDecimal parseAmount(String amountStr) {
        // Vervang komma door punt voor de BigDecimal conversie
        String normalizedAmount = amountStr.replace(",", ".");
        return new BigDecimal(normalizedAmount);
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<?> bulkUpdateTransactions(@RequestBody List<Transaction> updatedTransactions) {
        for (Transaction updated : updatedTransactions) {
            Optional<Transaction> existingOpt = transactionService.getTransactionById(updated.getTransactions_id());

            if (existingOpt.isPresent()) {
                Transaction existing = existingOpt.get();
                existing.setAccount(updated.getAccount());
                existing.setRecipient(updated.getRecipient());
                existing.setDescription(updated.getDescription());
                existing.setAmount(updated.getAmount());
                existing.setCategory(updated.getCategory());
                existing.setDate(updated.getDate());

                transactionService.saveTransaction(existing);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Transacties succesvol geüpdatet");
        return ResponseEntity.ok(response);
    }

}