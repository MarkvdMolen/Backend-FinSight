package com.finsight.TransactionService.dto;

import java.math.BigDecimal;

public record CategoryTotalDTO(
        String category,
        BigDecimal total      // altijd positief
) {}
