package com.finsight.wealth.service;

import com.finsight.wealth.dto.OverviewDTO;
import com.finsight.wealth.entity.Account;
import com.finsight.wealth.entity.BalanceSnapshot;
import com.finsight.wealth.entity.Category;
import com.finsight.wealth.entity.FinancialTransaction;
import com.finsight.wealth.repository.AccountRepository;
import com.finsight.wealth.repository.BalanceSnapshotRepository;
import com.finsight.wealth.repository.CategoryRepository;
import com.finsight.wealth.repository.FinancialTransactionRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WealthApplicationService {

    private final AccountRepository accountRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final WealthBusinessService wealthBusinessService;

    public WealthApplicationService(
            final AccountRepository accountRepository,
            final BalanceSnapshotRepository balanceSnapshotRepository,
            final FinancialTransactionRepository financialTransactionRepository,
            final CategoryRepository categoryRepository,
            final WealthBusinessService wealthBusinessService
    ) {
        this.accountRepository = accountRepository;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.financialTransactionRepository = financialTransactionRepository;
        this.categoryRepository = categoryRepository;
        this.wealthBusinessService = wealthBusinessService;
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll(Sort.by("id"));
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    public List<BalanceSnapshot> getSnapshots(final LocalDate date) {
        return balanceSnapshotRepository
                .findAllByDateLessThanEqualOrderByDateAsc(date);
    }

    public List<FinancialTransaction> getTransactions(final YearMonth month) {
        return financialTransactionRepository.findAllByBookingDateBetween(
                month.atDay(1),
                month.atEndOfMonth()
        );
    }

    public OverviewDTO getDashboard(final YearMonth month) {
        final List<Account> accounts = getAccounts();

        final List<BalanceSnapshot> snapshots =
                getSnapshots(month.atEndOfMonth());

        final List<FinancialTransaction> transactions =
                getTransactions(month);

        return wealthBusinessService.calculateDashboard(
                month,
                accounts,
                snapshots,
                transactions
        );
    }
}
