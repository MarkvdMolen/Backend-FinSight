package com.finsight.TransactionService.Repository;

import com.finsight.TransactionService.Repository.Projections.CategoryTotalView;
import com.finsight.TransactionService.Repository.Projections.MonthlyTrendView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsRepository extends Repository<com.finsight.TransactionService.Entity.Transaction, Long> {

    // =========================
    // Monthly trend (income vs expenses)
    // =========================
    @Query(value = """
      with base as (
        select date_trunc('month', t.date) as month,
               t.amount,
               t.category
        from transactions t
        where t.date between :start and :end
      )
      select
        month as month,
        coalesce(sum(case when amount > 0 then amount else 0 end), 0)  as income,
        coalesce(sum(case when amount < 0 then -amount else 0 end), 0) as expenses
      from base
      where (:applyExcl = false OR category not in (:excluded))
      group by month
      order by month
      """, nativeQuery = true)
    List<MonthlyTrendView> monthlyTrend(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excluded") List<String> excluded,
            @Param("applyExcl") boolean applyExcl
    );

    // =========================
    // Expenses by category (positief gerapporteerd)
    // =========================
    @Query(value = """
      select category as category,
             sum(-amount) as total
      from transactions
      where date between :start and :end
        and amount < 0
        and (:applyExcl = false OR category not in (:excluded))
      group by category
      order by sum(-amount) desc
      """, nativeQuery = true)
    List<CategoryTotalView> expensesByCategory(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excluded") List<String> excluded,
            @Param("applyExcl") boolean applyExcl
    );

    // =========================
    // Income by category
    // =========================
    @Query(value = """
      select category as category,
             sum(amount) as total
      from transactions
      where date between :start and :end
        and amount > 0
        and (:applyExcl = false OR category not in (:excluded))
      group by category
      order by sum(amount) desc
      """, nativeQuery = true)
    List<CategoryTotalView> incomeByCategory(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excluded") List<String> excluded,
            @Param("applyExcl") boolean applyExcl
    );
}