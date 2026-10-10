package com.finsight.wealth.repository;

import com.finsight.wealth.entity.FinancialTransaction;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    List<FinancialTransaction> findAllByBookingDateBetween(LocalDate start, LocalDate end);
}
