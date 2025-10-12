package com.finsight.TransactionService.Service;

import com.finsight.TransactionService.Entity.Transaction;
import com.finsight.TransactionService.dto.*;
import com.finsight.TransactionService.Repository.AnalyticsRepository;
import com.finsight.TransactionService.Repository.TransactionRepository;
import com.finsight.TransactionService.Repository.Projections.CategoryTotalView;
import com.finsight.TransactionService.Repository.Projections.MonthlyTrendView;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final TransactionRepository transactionRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository,
                            TransactionRepository transactionRepository) {
        this.analyticsRepository = analyticsRepository;
        this.transactionRepository = transactionRepository;
    }

    // ===== Defaults voor datums =====
    public record DateRange(LocalDate start, LocalDate end) {}
    public DateRange defaulted(LocalDate start, LocalDate end) {
        LocalDate e = (end == null) ? LocalDate.now() : end;
        LocalDate s = (start == null) ? e.minusMonths(6).withDayOfMonth(1) : start;
        if (s.isAfter(e)) throw new IllegalArgumentException("start must be <= end");
        return new DateRange(s, e);
    }

    private boolean applyExcl(List<String> excluded) {
        return excluded != null && !excluded.isEmpty();
    }

    // ===== TREND =====
    public List<MonthlyTrendDTO> monthlyTrend(LocalDate start, LocalDate end, List<String> excluded) {
        DateRange r = defaulted(start, end);
        List<MonthlyTrendView> rows = analyticsRepository.monthlyTrend(r.start, r.end, safeList(excluded), applyExcl(excluded));
        return rows.stream().map(this::mapTrend).toList();
    }

    private MonthlyTrendDTO mapTrend(MonthlyTrendView v) {
        return new MonthlyTrendDTO(v.getMonth(), nz(v.getIncome()), nz(v.getExpenses()));
        // income/expenses komen al positief uit de query
    }

    // ===== CATEGORY BREAKDOWN =====
    public List<CategoryTotalDTO> expensesByCategory(LocalDate start, LocalDate end, List<String> excluded) {
        DateRange r = defaulted(start, end);
        return analyticsRepository.expensesByCategory(r.start, r.end, safeList(excluded), applyExcl(excluded))
                .stream().map(this::mapCat).toList();
    }

    public List<CategoryTotalDTO> incomeByCategory(LocalDate start, LocalDate end, List<String> excluded) {
        DateRange r = defaulted(start, end);
        return analyticsRepository.incomeByCategory(r.start, r.end, safeList(excluded), applyExcl(excluded))
                .stream().map(this::mapCat).toList();
    }

    private CategoryTotalDTO mapCat(CategoryTotalView v) {
        return new CategoryTotalDTO(v.getCategory(), nz(v.getTotal()));
    }

    // ===== OVERVIEW SUMMARY =====
    public OverviewSummaryDTO overview(LocalDate start, LocalDate end, List<String> excluded) {
        var trend = monthlyTrend(start, end, excluded);
        BigDecimal income = trend.stream().map(MonthlyTrendDTO::income).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses = trend.stream().map(MonthlyTrendDTO::expenses).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OverviewSummaryDTO(income, expenses, income.subtract(expenses));
    }

    // ===== helpers =====
    private static List<String> safeList(List<String> in) { return (in == null) ? List.of() : in; }
    private static BigDecimal nz(BigDecimal b) { return (b == null) ? BigDecimal.ZERO : b; }
}
