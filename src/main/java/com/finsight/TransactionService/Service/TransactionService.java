package com.finsight.TransactionService.Service;

import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // Methode om alle transacties op te halen
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // Methode om een transactie op te halen op basis van het ID
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
