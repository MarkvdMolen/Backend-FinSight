package com.finsight.wealth.dto;

import java.math.BigDecimal;
import java.util.List;

public record OverviewDTO(
        String asOf,
        String monthLabel,
        BigDecimal income,
        BigDecimal expenses,
        List<AccountDTO> accounts,
        List<HistoryDTO> history,
        boolean balanceOnly
) {
    public OverviewDTO(String asOf, String monthLabel, BigDecimal income, BigDecimal expenses,
                       List<AccountDTO> accounts, List<HistoryDTO> history) {
        this(asOf, monthLabel, income, expenses, accounts, history, false);
    }
}
