package com.finsight.TransactionSummaryService.Controller;

import com.finsight.TransactionSummaryService.Entity.YearlyCategorySummaryDTO;

import com.finsight.TransactionSummaryService.Entity.YearlySummaryDTO;
import com.finsight.TransactionSummaryService.Service.TransactionSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/summary")
@Tag(
        name = "Grouped Transactions",
        description = "ETL for Grouped Transactions"
)
public class TransactionSummaryController {

    @Autowired
    private TransactionSummaryService service;

    @GetMapping("/byCategory/{year}")
    public ResponseEntity<List<YearlyCategorySummaryDTO>> getYearlySummaryByCategory(
            @PathVariable int year) {
        return ResponseEntity.ok(service.getYearlySummaryByCategory(year));
    }

    @GetMapping("/{year}")
    public ResponseEntity<List<YearlySummaryDTO>> getYearlySummary(
            @PathVariable int year) {
        return ResponseEntity.ok(service.getYearlySummary(year));
    }

}