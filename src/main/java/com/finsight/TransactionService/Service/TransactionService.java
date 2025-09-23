package com.finsight.TransactionService.Service;

import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.Repository.TransactionRepository;
import com.finsight.TransactionService.dto.SearchCriteriaDTO;
import com.finsight.TransactionService.Spec.TransactionSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieve transactions with optional filtering, sorting, and pagination.
     * If no filter criteria is provided, fetch all transactions with pagination and sorting.
     * @param criteria The string to filter the transactions by recipient or description.
     * @param pageable The pagination and sorting configuration.
     * @return A paginated and sorted page of transactions.
     */
    public Page<Transaction> findTransactions(
            SearchCriteriaDTO criteria,
            Pageable pageable) {
        // Bouw de Specification op uit de DTO
        Specification<Transaction> spec = TransactionSpecification.byCriteria(criteria);
        // findAll(spec, pageable) dekt zowel filteren als pagineren/ sorteren
        return transactionRepository.findAll(spec, pageable);
    }

    /**
     * Oude methode, nog beschikbaar voor eenvoudige filter-string.
     * Je kunt callers migreren naar de nieuwe findTransactions(criteria, pageable).
     */
    public Page<Transaction> findTransactions(
            String filterCriteria,
            Pageable pageable) {
        if (filterCriteria == null || filterCriteria.isEmpty()) {
            return transactionRepository.findAll(pageable);
        } else {
            return transactionRepository
                    .findByRecipientContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            filterCriteria,
                            filterCriteria,
                            pageable);
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
        Transaction saved = transactionRepository.save(transaction);

        // Set the id first, then if group_id = null copy the value into group_id
        if (saved.getGroup_id() == null) {
            saved.setGroup_id(saved.getTransactionsId());
            saved = transactionRepository.save(saved);
        }

        return saved;
    }

    public boolean existsByRowHash(String rowHash) {
        return transactionRepository.existsByRowHash(rowHash);
    }

    public long countByCategoryIsNotNull() {
        return transactionRepository.countByCategoryIsNotNull();
    }

    public long countAllTransactions() {
        return transactionRepository.count();
    }
}
