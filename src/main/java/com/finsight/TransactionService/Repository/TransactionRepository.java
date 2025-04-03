package com.finsight.TransactionService.Repository;

import com.finsight.TransactionService.Entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Filters transactions by recipient or description (case-insensitive) with pagination and sorting.
     * @param recipient The recipient filter criteria.
     * @param description The description filter criteria.
     * @param pageable The pagination and sorting configuration.
     * @return A paginated and filtered list of transactions.
     */
    Page<Transaction> findByRecipientContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String recipient, String description, Pageable pageable);
}
