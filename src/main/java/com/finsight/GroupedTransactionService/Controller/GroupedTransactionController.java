package com.finsight.GroupedTransactionService.Controller;

import com.finsight.GroupedTransactionService.Entity.GroupedTransactionDTO;
import com.finsight.GroupedTransactionService.Service.GroupedTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/grouped-transactions")
@Tag(
        name = "Grouped Transactions",
        description = "ETL for Grouped Transactions"
)
public class GroupedTransactionController {

    @Autowired
    private GroupedTransactionService groupedTransactionService;

    /**
     * Trigger ETL
     */
    @PostMapping("/etl")
    public ResponseEntity<Map<String, String>> runEtl() {
        groupedTransactionService.runGroupedTransactionsETL();
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

}