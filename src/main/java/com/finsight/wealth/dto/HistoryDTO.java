package com.finsight.wealth.dto;

import java.math.BigDecimal;

public record HistoryDTO(
        String month,
        String label,
        BigDecimal value
) {
}
