package com.finsight.TransactionService.Controller;
import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.Entity.TransactionCSV;
import com.finsight.TransactionService.Service.TransactionService;
import com.finsight.UtilClasses.HashUtil;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(
        name = "Transactions",
        description = "Provides standard CRUD operations and additional endpoints to retrieve specific details " +
                "for bank statement transactions."
)
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
    @Operation(
            summary = "Retrieve a paginated list of transactions",
            description = "Returns a pagination object containing metadata (page, size, totalElements, totalPages) "
                    + "and the actual list of transactions in the `content` field. <br>"
                    + "Use query parameters to control pagination."
    )
    @ApiResponse(responseCode = "200", description = "PageTransaction with a list of transactions in the content.")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters (NO IMPLEMENTATION YET)", content = @Content)
    @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)

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


    @Operation(
            summary = "Update an existing Transaction",
            description = "Request to update the details of a specific transaction identified by its ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transaction successfully updated",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Transaction.class)
            )
    )
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server Error", content = @Content)
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @Parameter(description = "Transaction ID")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update an existing Transaction",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Transaction.class)
                    )
            )
            @RequestBody Transaction updatedTransaction) {

        Optional<Transaction> existingTransactionOpt = transactionService.getTransactionById(id);

        if (existingTransactionOpt.isPresent()) {
            Transaction existingTransaction = existingTransactionOpt.get();

            // Update de velden van de bestaande transactie met de nieuwe waarden
            existingTransaction.setAccount(updatedTransaction.getAccount());
            existingTransaction.setClassificationSource(updatedTransaction.getClassificationSource());
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

    @Operation(
            summary = "Uploads a CSV file",
            description = "Uploads a CSV file containing transaction data to be processed and stored."
    )
    @ApiResponse(responseCode = "200", description = "File uploaded and processed successfully!", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid file format or empty file", content = @Content)
    @ApiResponse(responseCode = "405", description = "Wrong Request Type, should be `POST`", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server Error", content = @Content)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCsvFile(
        @Parameter(
                description = "The CSV file to upload",
                required = true,
                content = @Content(
                        mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        schema = @Schema(type = "string", format = "binary")
                )
        )
        @RequestParam("csv file") MultipartFile file) {

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
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Pas dit aan als het CSV-bestand een ander datumformaat gebruikt

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
                            .classificationSource(0)
                            .build();

                    transactionService.saveTransaction(transaction);
                }
            }

            return ResponseEntity.ok("File uploaded and processed successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();  // Debuggen for errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Given file is of wrong format");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + ex.getMessage());
        }
    }

    private BigDecimal parseAmount(String amountStr) {
        // Vervang komma door punt voor de BigDecimal conversie
        String normalizedAmount = amountStr.replace(",", ".");
        return new BigDecimal(normalizedAmount);
    }

    @Operation(
            summary = "Bulk update transactions",
            description = "Updates multiple existing transactions at once based on the provided list. "
                    + "Each transaction in the request body must contain a valid `transactionsId`. "
                    + "Only existing transactions will be updated; missing IDs will be ignored."
    )
    @ApiResponse(responseCode = "200", description = "Transactions successfully updated", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., malformed JSON)", content = @Content)
    @ApiResponse(responseCode = "404", description = "One or more transactions not found", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    @PutMapping("/bulk-update")
    public ResponseEntity<?> bulkUpdateTransactions(@RequestBody List<Transaction> updatedTransactions) {
        for (Transaction updated : updatedTransactions) {
            Optional<Transaction> existingOpt = transactionService.getTransactionById(updated.getTransactionsId());

            if (existingOpt.isPresent()) {
                Transaction existing = existingOpt.get();
                existing.setAccount(updated.getAccount());
                existing.setRecipient(updated.getRecipient());
                existing.setDescription(updated.getDescription());
                existing.setAmount(updated.getAmount());
                existing.setClassificationSource(updated.getClassificationSource());
                existing.setCategory(updated.getCategory());
                existing.setDate(updated.getDate());

                transactionService.saveTransaction(existing);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Transacties succesvol geüpdatet");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get categorized transactions count",
            description = "Returns the number of transactions that have a category assigned, "
                    + "along with the total number of transactions. <br>"
                    + "Where count represents the categorized transactions."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved counts",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"total\": 450, \"count\": 120 }")
            )
    )
    @ApiResponse(responseCode = "500", description = "Internal server Error", content = @Content)
    @GetMapping("/count-categorized")
    public ResponseEntity<Map<String, Long>> countUncategorizedTransactions() {
        long count = transactionService.countByCategoryIsNotNull();
        long totalTransactions = transactionService.countAllTransactions();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        response.put("total", totalTransactions);

        return ResponseEntity.ok(response);
    }


}