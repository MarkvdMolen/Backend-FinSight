package com.finsight.TransactionService.Repository.Projections;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlyTrendView {
    LocalDate getMonth();
    BigDecimal getIncome();
    BigDecimal getExpenses();
}
