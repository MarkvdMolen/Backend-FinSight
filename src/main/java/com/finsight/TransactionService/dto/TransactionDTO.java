package com.finsight.TransactionService.dto;

import com.finsight.TransactionService.Entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
        Long id,
        String account,
        BigDecimal amount,
        String category,
        String classificationSource, // als String gewenst; anders Integer laten
        LocalDate date,
        String description,
        String recipient,
        String rowHash,
        Long groupId,
        boolean income,   // afgeleid: amount > 0
        boolean expense   // afgeleid: amount < 0
) {
    public static TransactionDTO from(Transaction t) {
        return new TransactionDTO(
                t.getTransactionsId(),
                t.getAccount(),
                t.getAmount(),
                t.getCategory(),
                String.valueOf(t.getClassificationSource()),
                t.getDate(),
                t.getDescription(),
                t.getRecipient(),
                t.getRowHash(),
                t.getGroupId(),
                t.isIncome(),
                t.isExpense()
        );
    }
}

