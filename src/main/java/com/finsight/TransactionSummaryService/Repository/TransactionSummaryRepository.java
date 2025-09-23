package com.finsight.TransactionSummaryService.Repository;

import com.finsight.TransactionService.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionSummaryRepository extends JpaRepository<Transaction, Long> {

    @Query(
            value = "SELECT * FROM get_yearly_category_summary(:year)",
            nativeQuery = true
    )
    List<Object[]> getYearlyCategorySummary(@Param("year") int year);

    @Query(
            value = "SELECT * FROM get_yearly_summary(:year)",
            nativeQuery = true
    )
    List<Object[]> getYearlySummary(@Param("year") int year);

}
