package com.finsight.wealth.service;

import com.finsight.wealth.dto.AccountDTO;
import com.finsight.wealth.dto.OverviewDTO;
import com.finsight.wealth.dto.HistoryDTO;
import com.finsight.wealth.entity.Account;
import com.finsight.wealth.entity.BalanceSnapshot;
import com.finsight.wealth.entity.FinancialTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WealthBusinessService {

    private static final Locale NL = Locale.forLanguageTag("nl-NL");

    public OverviewDTO calculateDashboard(
            final YearMonth month,
            final List<Account> accounts,
            final List<BalanceSnapshot> snapshots,
            final List<FinancialTransaction> transactions
    ) {
        validateCurrencies(accounts);

        final Map<Long, BigDecimal> latestBalances =
                determineLatestBalances(snapshots);

        validateBalances(accounts, latestBalances);

        final List<AccountDTO> accountViews =
                createAccountViews(accounts, latestBalances);

        final List<HistoryDTO> history =
                createHistory(month, accounts, snapshots);

        final BigDecimal income = calculateIncome(transactions);
        final BigDecimal expenses = calculateExpenses(transactions);

        final LocalDate asOf = determineAsOfDate(
                snapshots,
                month.atEndOfMonth()
        );

        return new OverviewDTO(
                asOf.format(DateTimeFormatter.ofPattern("d MMMM yyyy", NL)),
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy", NL)),
                income,
                expenses,
                accountViews,
                history
        );
    }

    private void validateCurrencies(final List<Account> accounts) {
        final boolean containsUnsupportedCurrency = accounts.stream()
                .anyMatch(account -> !"EUR".equals(account.getCurrency()));

        if (containsUnsupportedCurrency) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Valutaomrekening is nog niet beschikbaar; het dashboard ondersteunt alleen EUR."
            );
        }
    }

    private Map<Long, BigDecimal> determineLatestBalances(
            final List<BalanceSnapshot> snapshots
    ) {
        final Map<Long, BigDecimal> latestBalances = new HashMap<>();

        snapshots.forEach(snapshot ->
                latestBalances.put(
                        snapshot.getAccount().getId(),
                        snapshot.getBalance()
                )
        );

        return latestBalances;
    }

    private void validateBalances(
            final List<Account> accounts,
            final Map<Long, BigDecimal> latestBalances
    ) {
        final Set<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toSet());

        if (!latestBalances.keySet().containsAll(accountIds)) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Er ontbreken rekeningstanden voor deze periode."
            );
        }
    }

    private List<AccountDTO> createAccountViews(
            final List<Account> accounts,
            final Map<Long, BigDecimal> latestBalances
    ) {
        final List<AccountDTO> views = new ArrayList<>();

        for (final Account account : accounts) {
            final BigDecimal value = latestBalances.get(account.getId());
            final boolean cash =
                    account.getType() == Account.AccountType.CASH;

            views.add(
                    new AccountDTO(
                            account.getId().toString(),
                            account.getInstitution(),
                            account.getName(),
                            cash ? value : BigDecimal.ZERO,
                            cash ? BigDecimal.ZERO : value,
                            "#238477"
                    )
            );
        }

        return views;
    }

    private List<HistoryDTO> createHistory(
            final YearMonth month,
            final List<Account> accounts,
            final List<BalanceSnapshot> snapshots
    ) {
        final List<HistoryDTO> history = new ArrayList<>();

        final Set<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toSet());

        for (int i = 11; i >= 0; i--) {
            final YearMonth period = month.minusMonths(i);

            final Map<Long, BigDecimal> balancesAtMonth =
                    new HashMap<>();

            snapshots.stream()
                    .filter(snapshot ->
                            !snapshot.getDate()
                                    .isAfter(period.atEndOfMonth())
                    )
                    .forEach(snapshot ->
                            balancesAtMonth.put(
                                    snapshot.getAccount().getId(),
                                    snapshot.getBalance()
                            )
                    );

            if (balancesAtMonth.keySet().containsAll(accountIds)) {
                final BigDecimal total = balancesAtMonth.values()
                        .stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                history.add(
                        new HistoryDTO(
                                period.toString(),
                                period.format(
                                        DateTimeFormatter.ofPattern("MMM", NL)
                                ),
                                total
                        )
                );
            }
        }

        return history;
    }

    private BigDecimal calculateIncome(
            final List<FinancialTransaction> transactions
    ) {
        return transactions.stream()
                .filter(transaction -> !transaction.isInternalTransfer())
                .map(FinancialTransaction::getAmount)
                .filter(amount -> amount.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateExpenses(
            final List<FinancialTransaction> transactions
    ) {
        return transactions.stream()
                .filter(transaction -> !transaction.isInternalTransfer())
                .map(FinancialTransaction::getAmount)
                .filter(amount -> amount.signum() < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate determineAsOfDate(
            final List<BalanceSnapshot> snapshots,
            final LocalDate fallback
    ) {
        return snapshots.stream()
                .map(BalanceSnapshot::getDate)
                .max(LocalDate::compareTo)
                .orElse(fallback);
    }
}
