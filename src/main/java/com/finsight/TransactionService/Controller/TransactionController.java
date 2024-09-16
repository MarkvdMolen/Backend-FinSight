package com.finsight.TransactionService.Controller;
import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}