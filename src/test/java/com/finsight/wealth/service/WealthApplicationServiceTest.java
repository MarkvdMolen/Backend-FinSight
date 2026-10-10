package com.finsight.wealth.service;

import com.finsight.wealth.dto.OverviewDTO;
import com.finsight.wealth.entity.*;
import com.finsight.wealth.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WealthApplicationServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BalanceSnapshotRepository balanceSnapshotRepository;
    @Mock private FinancialTransactionRepository financialTransactionRepository;
    @Mock private WealthBusinessService wealthBusinessService;
    @InjectMocks private WealthApplicationService service;

    @Test
    void retrievesAccountsSortedById() {
        List<Account> accounts = List.of(new Account());
        when(accountRepository.findAll(Sort.by("id"))).thenReturn(accounts);

        assertSame(accounts, service.getAccounts());
        verify(accountRepository).findAll(Sort.by("id"));
    }

    @Test
    void retrievesCategoriesSortedByName() {
        List<Category> categories = List.of(new Category());
        when(categoryRepository.findAll(Sort.by("name"))).thenReturn(categories);

        assertSame(categories, service.getCategories());
        verify(categoryRepository).findAll(Sort.by("name"));
    }

    @Test
    void retrievesSnapshotsUpToRequestedDateInChronologicalOrder() {
        LocalDate date = LocalDate.of(2026, 10, 10);
        List<BalanceSnapshot> snapshots = List.of(new BalanceSnapshot());
        when(balanceSnapshotRepository.findAllByDateLessThanEqualOrderByDateAsc(date))
                .thenReturn(snapshots);

        assertSame(snapshots, service.getSnapshots(date));
        verify(balanceSnapshotRepository).findAllByDateLessThanEqualOrderByDateAsc(date);
    }

    @Test
    void includesLeapDayInTransactionQuery() {
        List<FinancialTransaction> transactions = List.of(new FinancialTransaction());
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 2, 29);
        when(financialTransactionRepository.findAllByBookingDateBetween(start, end))
                .thenReturn(transactions);

        assertSame(transactions, service.getTransactions(YearMonth.of(2024, 2)));
        verify(financialTransactionRepository).findAllByBookingDateBetween(start, end);
    }

    @Test
    void passesRepositoryDataForSelectedMonthToBusinessService() {
        YearMonth month = YearMonth.of(2026, 10);
        List<Account> accounts = List.of(new Account());
        List<BalanceSnapshot> snapshots = List.of(new BalanceSnapshot());
        List<FinancialTransaction> transactions = List.of(new FinancialTransaction());
        OverviewDTO expected = new OverviewDTO("31 oktober 2026", "oktober 2026",
                BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
        when(accountRepository.findAll(Sort.by("id"))).thenReturn(accounts);
        when(balanceSnapshotRepository.findAllByDateLessThanEqualOrderByDateAsc(month.atEndOfMonth()))
                .thenReturn(snapshots);
        when(financialTransactionRepository.findAllByBookingDateBetween(month.atDay(1), month.atEndOfMonth()))
                .thenReturn(transactions);
        when(wealthBusinessService.calculateDashboard(month, accounts, snapshots, transactions))
                .thenReturn(expected);

        assertSame(expected, service.getDashboard(month));
        verify(wealthBusinessService).calculateDashboard(month, accounts, snapshots, transactions);
        verifyNoInteractions(categoryRepository);
    }
    @Test
    void linkedDashboardExcludesDemoBalancesAndDoesNotFetchTransactions() {
        YearMonth month = YearMonth.of(2026, 10);
        Account demo = new Account();
        demo.setId(1L);
        Account live = new Account();
        live.setId(2L);
        live.setBankConnection(new com.finsight.banking.entity.BankConnection());
        BalanceSnapshot demoSnapshot = new BalanceSnapshot();
        demoSnapshot.setAccount(demo);
        BalanceSnapshot liveSnapshot = new BalanceSnapshot();
        liveSnapshot.setAccount(live);
        when(accountRepository.findAll(Sort.by("id"))).thenReturn(List.of(demo, live));
        when(balanceSnapshotRepository.findAllByDateLessThanEqualOrderByDateAsc(month.atEndOfMonth()))
                .thenReturn(List.of(demoSnapshot, liveSnapshot));
        OverviewDTO calculated = new OverviewDTO("10 oktober 2026", "oktober 2026",
                BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of());
        when(wealthBusinessService.calculateDashboard(month, List.of(live), List.of(liveSnapshot), List.of()))
                .thenReturn(calculated);

        OverviewDTO result = service.getDashboard(month);

        org.junit.jupiter.api.Assertions.assertTrue(result.balanceOnly());
        verify(wealthBusinessService).calculateDashboard(month, List.of(live), List.of(liveSnapshot), List.of());
        verifyNoInteractions(financialTransactionRepository);
    }
}
