package com.finsight.wealth.service;

import com.finsight.wealth.entity.Account;
import com.finsight.wealth.entity.BalanceSnapshot;
import com.finsight.wealth.entity.FinancialTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WealthBusinessServiceTest {
    private static final YearMonth MONTH = YearMonth.of(2026, 10);
    private final WealthBusinessService service = new WealthBusinessService();

    @Test
    void usesLatestBalancePerAccountAndSeparatesCashFromInvestments() {
        Account cash = account(1L, Account.AccountType.CASH);
        Account investments = account(2L, Account.AccountType.INVESTMENT);
        // The repository contract supplies snapshots in ascending date order.
        var dashboard = service.calculateDashboard(MONTH, List.of(cash, investments), List.of(
                snapshot(cash, "2026-09-30", "100.00"),
                snapshot(investments, "2026-09-30", "200.00"),
                snapshot(cash, "2026-10-01", "150.25"),
                snapshot(cash, "2026-10-10", "180.55")
        ), List.of());

        var cashView = dashboard.accounts().get(0);
        var investmentView = dashboard.accounts().get(1);
        assertAll(
                () -> assertEquals("1", cashView.id()),
                () -> assertEquals("Testbank", cashView.institution()),
                () -> assertEquals("Rekening 1", cashView.name()),
                () -> assertAmount("180.55", cashView.cash()),
                () -> assertAmount("0", cashView.investments()),
                () -> assertAmount("0", investmentView.cash()),
                () -> assertAmount("200.00", investmentView.investments()),
                () -> assertEquals("10 oktober 2026", dashboard.asOf()),
                () -> assertEquals("oktober 2026", dashboard.monthLabel())
        );
    }

    @Test
    void sumsExternalIncomeAndExpensesWithoutCountingInternalTransfers() {
        Account account = account(1L, Account.AccountType.CASH);
        var dashboard = service.calculateDashboard(MONTH, List.of(account),
                List.of(snapshot(account, "2026-10-01", "0.00")), List.of(
                        transaction("1200.55", false),
                        transaction("0.45", false),
                        transaction("-300.25", false),
                        transaction("-10.05", false),
                        transaction("999.99", true),
                        transaction("-999.99", true),
                        transaction("0.00", false)
                ));

        assertAmount("1201.00", dashboard.income());
        assertAmount("310.30", dashboard.expenses());
    }

    @Test
    void startsHistoryWhenAllAccountsHaveBalancesAndCarriesBalancesForward() {
        Account cash = account(1L, Account.AccountType.CASH);
        Account investments = account(2L, Account.AccountType.INVESTMENT);
        var dashboard = service.calculateDashboard(MONTH, List.of(cash, investments), List.of(
                snapshot(cash, "2026-07-31", "100.00"),
                snapshot(investments, "2026-08-31", "250.00"),
                snapshot(cash, "2026-09-30", "150.00"),
                snapshot(investments, "2026-10-31", "275.00")
        ), List.of());

        assertEquals(List.of("2026-08", "2026-09", "2026-10"),
                dashboard.history().stream().map(point -> point.month()).toList());
        assertAmount("350.00", dashboard.history().get(0).value());
        assertAmount("400.00", dashboard.history().get(1).value());
        assertAmount("425.00", dashboard.history().get(2).value());
    }

    @Test
    void limitsHistoryToTwelveMonthsEvenWhenOlderBalancesExist() {
        Account account = account(1L, Account.AccountType.CASH);
        var dashboard = service.calculateDashboard(MONTH, List.of(account),
                List.of(snapshot(account, "2024-01-01", "50.00")), List.of());

        assertEquals(12, dashboard.history().size());
        assertEquals("2025-11", dashboard.history().get(0).month());
        assertEquals("2026-10", dashboard.history().get(11).month());
        dashboard.history().forEach(point -> assertAmount("50.00", point.value()));
        assertAmount("0", dashboard.income());
        assertAmount("0", dashboard.expenses());
    }

    @Test
    void rejectsMissingBalanceForAnyAccount() {
        Account cash = account(1L, Account.AccountType.CASH);
        Account investments = account(2L, Account.AccountType.INVESTMENT);
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.calculateDashboard(MONTH, List.of(cash, investments),
                        List.of(snapshot(cash, "2026-10-01", "0.00")), List.of()));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
        assertEquals("Er ontbreken rekeningstanden voor deze periode.", exception.getReason());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"USD", "GBP", "eur"})
    void rejectsUnsupportedCurrencies(String currency) {
        Account account = account(1L, Account.AccountType.CASH);
        account.setCurrency(currency);
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.calculateDashboard(MONTH, List.of(account),
                        List.of(snapshot(account, "2026-10-01", "100.00")), List.of()));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
        assertTrue(exception.getReason().contains("EUR"));
    }

    @Test
    void returnsNoAccountsAndZeroCashflowForEmptyInput() {
        var dashboard = service.calculateDashboard(MONTH, List.of(), List.of(), List.of());

        assertTrue(dashboard.accounts().isEmpty());
        assertAmount("0", dashboard.income());
        assertAmount("0", dashboard.expenses());
        assertEquals("31 oktober 2026", dashboard.asOf());
    }

    private static Account account(Long id, Account.AccountType type) {
        Account account = new Account();
        account.setId(id);
        account.setInstitution("Testbank");
        account.setName("Rekening " + id);
        account.setCurrency("EUR");
        account.setType(type);
        return account;
    }

    private static BalanceSnapshot snapshot(Account account, String date, String balance) {
        BalanceSnapshot snapshot = new BalanceSnapshot();
        snapshot.setAccount(account);
        snapshot.setDate(LocalDate.parse(date));
        snapshot.setBalance(new BigDecimal(balance));
        return snapshot;
    }

    private static FinancialTransaction transaction(String amount, boolean internal) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setAmount(new BigDecimal(amount));
        transaction.setInternalTransfer(internal);
        return transaction;
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "Expected " + expected + " but was " + actual);
    }
}
