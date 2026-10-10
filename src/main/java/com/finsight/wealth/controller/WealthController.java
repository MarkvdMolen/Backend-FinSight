package com.finsight.wealth.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.finsight.wealth.dto.OverviewDTO;
import com.finsight.wealth.entity.Account;
import com.finsight.wealth.entity.BalanceSnapshot;
import com.finsight.wealth.entity.Category;
import com.finsight.wealth.entity.FinancialTransaction;

import com.finsight.wealth.service.WealthApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/wealth")
public class WealthController {

    private final WealthApplicationService wealthApplicationService;

    public WealthController(final WealthApplicationService wealthApplicationService) {
        this.wealthApplicationService = wealthApplicationService;
    }

    @GetMapping("/dashboard")
    public OverviewDTO dashboard(
            @RequestParam(required = false) final String month
    ) {
        return wealthApplicationService.getDashboard(parseMonth(month));
    }

    @GetMapping("/accounts")
    public List<Account> accounts() {
        return wealthApplicationService.getAccounts();
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return wealthApplicationService.getCategories();
    }

    @GetMapping("/transactions")
    public List<FinancialTransaction> transactions(
            @RequestParam(required = false) final String month
    ) {
        return wealthApplicationService.getTransactions(parseMonth(month));
    }

    @GetMapping("/balance-snapshots")
    public List<BalanceSnapshot> snapshots(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate date
    ) {
        return wealthApplicationService.getSnapshots(
                date == null ? LocalDate.now() : date
        );
    }

    private YearMonth parseMonth(final String month) {
        try {
            return month == null
                    ? YearMonth.now()
                    : YearMonth.parse(month);
        } catch (final DateTimeParseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Gebruik maandformaat yyyy-MM"
            );
        }
    }
}
