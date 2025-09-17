package com.finsight.GroupedTransactionService.Repository;

import com.finsight.TransactionService.Entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupedTransactionRepository extends JpaRepository<Transaction, Long> {

}
