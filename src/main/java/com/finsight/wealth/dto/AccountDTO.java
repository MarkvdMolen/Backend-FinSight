package com.finsight.wealth.dto;

import java.math.BigDecimal;

public record AccountDTO(
        String id,
        String institution,
        String name,
        BigDecimal cash,
        BigDecimal investments,
        String color
) {
}