package com.finsight.TransactionSummaryService.Service;

import com.finsight.TransactionSummaryService.Entity.YearlyCategorySummaryDTO;
import com.finsight.TransactionSummaryService.Entity.YearlySummaryDTO;
import com.finsight.TransactionSummaryService.Repository.TransactionSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionSummaryService {

    @Autowired
    private TransactionSummaryRepository repository;

    public List<YearlyCategorySummaryDTO> getYearlySummaryByCategory(int year) {
        return repository.getYearlyCategorySummary(year).stream()
                .map(r -> new YearlyCategorySummaryDTO(
                        ((Number) r[0]).intValue(),   // year
                        ((String) r[1]).trim(),       // month
                        (String) r[2],                // category
                        ((Number) r[3]).longValue(),  // transactionCount
                        (BigDecimal) r[4],            // income
                        (BigDecimal) r[5]             // expense
                ))
                .toList();
    }

    public List<YearlySummaryDTO> getYearlySummary(int year) {
        return repository.getYearlySummary(year).stream()
                .map(r -> new YearlySummaryDTO(
                        ((Number) r[0]).intValue(),   // year
                        ((String) r[1]).trim(),       // month
                        (BigDecimal) r[2],            // income
                        (BigDecimal) r[3]             // expense
                ))
                .toList();
    }
}
