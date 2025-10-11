package com.finsight.TransactionService.Controller;

import com.finsight.TransactionService.dto.CategoryTotalDTO;
import com.finsight.TransactionService.dto.MonthlyTrendDTO;
import com.finsight.TransactionService.dto.OverviewSummaryDTO;
import com.finsight.TransactionService.Service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Totals for range", description = "Total income, total expenses (positive), and net savings.")
    public OverviewSummaryDTO summary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Start date (YYYY-MM-DD). Defaults to 1st day 6 months before end.")
            LocalDate start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "End date (YYYY-MM-DD). Defaults to today.")
            LocalDate end,

            @RequestParam(required = false, name = "excludeCategories")
            @Parameter(description = "Categories to exclude. Repeat param or comma-separated.")
            List<String> excludeCategories
    ) {
        List<String> excluded = normalizeExcludes(excludeCategories);
        return analyticsService.overview(start, end, excluded);
    }

    @GetMapping("/trend/monthly")
    @Operation(summary = "Monthly income vs expenses", description = "Buckets per calendar month; last/first month may be partial based on range.")
    public List<MonthlyTrendDTO> monthlyTrend(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            @RequestParam(required = false, name = "excludeCategories")
            List<String> excludeCategories
    ) {
        return analyticsService.monthlyTrend(start, end, normalizeExcludes(excludeCategories));
    }

    @GetMapping("/expenses/by-category")
    @Operation(summary = "Expenses by category", description = "Amounts reported as positive values.")
    public List<CategoryTotalDTO> expensesByCategory(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            @RequestParam(required = false, name = "excludeCategories")
            List<String> excludeCategories
    ) {
        return analyticsService.expensesByCategory(start, end, normalizeExcludes(excludeCategories));
    }

    @GetMapping("/income/by-category")
    @Operation(summary = "Income by category")
    public List<CategoryTotalDTO> incomeByCategory(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            @RequestParam(required = false, name = "excludeCategories")
            List<String> excludeCategories
    ) {
        return analyticsService.incomeByCategory(start, end, normalizeExcludes(excludeCategories));
    }

    /** Ondersteun zowel herhaalde params (?exclude=A&exclude=B) als CSV (?exclude=A,B) */
    private static List<String> normalizeExcludes(List<String> incoming) {
        if (incoming == null || incoming.isEmpty()) return List.of();
        if (incoming.size() == 1 && incoming.get(0) != null && incoming.get(0).contains(",")) {
            return Arrays.stream(incoming.get(0).split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return incoming.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
