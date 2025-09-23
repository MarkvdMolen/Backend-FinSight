package com.finsight.GroupedTransactionService.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GroupedTransactionDTO(
        Long id,
        BigDecimal amount,
        String category,
        LocalDate date,
        String description,
        String recipient
) {}

