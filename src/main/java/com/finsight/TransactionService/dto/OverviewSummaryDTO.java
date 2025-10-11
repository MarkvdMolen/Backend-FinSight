package com.finsight.TransactionService.dto;

import java.math.BigDecimal;

public record OverviewSummaryDTO(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netSavings
) {}