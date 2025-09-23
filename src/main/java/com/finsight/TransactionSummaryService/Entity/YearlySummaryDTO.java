package com.finsight.TransactionSummaryService.Entity;

import java.math.BigDecimal;

public record YearlySummaryDTO(
        int year,
        String month,
        BigDecimal income,
        BigDecimal expense
) {}
