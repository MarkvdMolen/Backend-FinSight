package com.finsight.TransactionService.Repository.Projections;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface MonthlyTrendView {
    OffsetDateTime getMonth();
    BigDecimal getIncome();
    BigDecimal getExpenses();
}
