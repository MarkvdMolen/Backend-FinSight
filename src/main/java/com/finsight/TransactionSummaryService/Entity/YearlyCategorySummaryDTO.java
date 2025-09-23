package com.finsight.TransactionSummaryService.Entity;

import java.math.BigDecimal;

public record YearlyCategorySummaryDTO(
        int year,
        String month,
        String category,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense
) {}

