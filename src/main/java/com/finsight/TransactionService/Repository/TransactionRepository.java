package com.finsight.TransactionService.Repository;

import com.finsight.TransactionService.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
