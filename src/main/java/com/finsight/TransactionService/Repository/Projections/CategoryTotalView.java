package com.finsight.TransactionService.Repository.Projections;

import java.math.BigDecimal;

public interface CategoryTotalView {
    String getCategory();
    BigDecimal getTotal();
}