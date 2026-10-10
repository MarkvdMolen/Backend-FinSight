package com.finsight.wealth.controller;

import com.finsight.wealth.dto.AccountDTO;
import com.finsight.wealth.dto.HistoryDTO;
import com.finsight.wealth.dto.OverviewDTO;
import com.finsight.wealth.service.WealthApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class WealthControllerTest {
    @Mock private WealthApplicationService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new WealthController(service)).build();
    }

    @Test
    void returnsDashboardJsonForRequestedMonth() throws Exception {
        YearMonth month = YearMonth.of(2026, 10);
        OverviewDTO dashboard = new OverviewDTO("31 oktober 2026", "oktober 2026",
                new BigDecimal("1200.55"), new BigDecimal("300.25"),
                List.of(new AccountDTO("1", "Testbank", "Cash",
                        new BigDecimal("100.25"), BigDecimal.ZERO, "#238477")),
                List.of(new HistoryDTO("2026-10", "okt", new BigDecimal("100.25"))));
        when(service.getDashboard(month)).thenReturn(dashboard);

        mvc.perform(get("/api/wealth/dashboard").param("month", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthLabel").value("oktober 2026"))
                .andExpect(jsonPath("$.asOf").value("31 oktober 2026"))
                .andExpect(jsonPath("$.income").value(1200.55))
                .andExpect(jsonPath("$.expenses").value(300.25))
                .andExpect(jsonPath("$.accounts[0].id").value("1"))
                .andExpect(jsonPath("$.accounts[0].cash").value(100.25))
                .andExpect(jsonPath("$.history[0].month").value("2026-10"));
        verify(service).getDashboard(month);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-13", "10-2026", "not-a-month", ""})
    void rejectsInvalidMonthBeforeCallingService(String month) throws Exception {
        mvc.perform(get("/api/wealth/dashboard").param("month", month))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/wealth/transactions").param("month", month))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void defaultsDashboardToCurrentMonth() throws Exception {
        YearMonth before = YearMonth.now();
        when(service.getDashboard(any(YearMonth.class))).thenReturn(
                new OverviewDTO("", "", BigDecimal.ZERO, BigDecimal.ZERO, List.of(), List.of()));

        mvc.perform(get("/api/wealth/dashboard")).andExpect(status().isOk());

        YearMonth after = YearMonth.now();
        ArgumentCaptor<YearMonth> month = ArgumentCaptor.forClass(YearMonth.class);
        verify(service).getDashboard(month.capture());
        assertTrue(month.getValue().equals(before) || month.getValue().equals(after));
    }

    @Test
    void preservesBusinessValidationStatus() throws Exception {
        when(service.getDashboard(YearMonth.of(2026, 10))).thenThrow(
                new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Ontbrekende rekeningstand"));

        mvc.perform(get("/api/wealth/dashboard").param("month", "2026-10"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void usesRequestedTransactionMonth() throws Exception {
        YearMonth month = YearMonth.of(2024, 2);
        when(service.getTransactions(month)).thenReturn(List.of());

        mvc.perform(get("/api/wealth/transactions").param("month", "2024-02"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(service).getTransactions(month);
    }

    @Test
    void parsesSnapshotDateAndRejectsInvalidDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 10);
        when(service.getSnapshots(date)).thenReturn(List.of());

        mvc.perform(get("/api/wealth/balance-snapshots").param("date", "2026-10-10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(service).getSnapshots(date);
        clearInvocations(service);

        mvc.perform(get("/api/wealth/balance-snapshots").param("date", "2026-02-30"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void defaultsSnapshotDateToToday() throws Exception {
        LocalDate before = LocalDate.now();
        when(service.getSnapshots(any(LocalDate.class))).thenReturn(List.of());

        mvc.perform(get("/api/wealth/balance-snapshots")).andExpect(status().isOk());

        LocalDate after = LocalDate.now();
        ArgumentCaptor<LocalDate> date = ArgumentCaptor.forClass(LocalDate.class);
        verify(service).getSnapshots(date.capture());
        assertTrue(date.getValue().equals(before) || date.getValue().equals(after));
    }
}
