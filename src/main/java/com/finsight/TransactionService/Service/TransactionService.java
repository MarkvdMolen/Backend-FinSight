package com.finsight.TransactionService.Service;

import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Retrieve transactions with optional filtering, sorting, and pagination.
     * If no filter criteria is provided, fetch all transactions with pagination and sorting.
     * @param filterCriteria The string to filter the transactions by recipient or description.
     * @param pageable The pagination and sorting configuration.
     * @return A paginated and sorted page of transactions.
     */
    public Page<Transaction> findTransactions(String filterCriteria, Pageable pageable) {
        if (filterCriteria == null || filterCriteria.isEmpty()) {
            // Fetch all transactions with pagination and sorting
            return transactionRepository.findAll(pageable);
        } else {
            // Fetch filtered transactions by recipient or description with pagination and sorting
            return transactionRepository.findByRecipientContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    filterCriteria, filterCriteria, pageable);
        }
    }

    /**
     * Retrieve a transaction by its ID.
     * @param id The ID of the transaction.
     * @return An Optional containing the transaction if found, or empty if not.
     */
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Save a new or existing transaction.
     * @param transaction The transaction to be saved.
     * @return The saved transaction.
     */
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
