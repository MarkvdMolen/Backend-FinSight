package com.finsight.TransactionService.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyTrendDTO(
        LocalDate month,      // eerste dag van de maand
        BigDecimal income,    // positief
        BigDecimal expenses   // positief
) {}
