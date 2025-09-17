package com.finsight.GroupedTransactionService.Controller;

import com.finsight.GroupedTransactionService.Entity.GroupedTransactionDTO;
import com.finsight.GroupedTransactionService.Service.GroupedTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(
            summary = "Run ETL for grouped transactions",
            description = """
        This endpoint executes a custom query that transfers data from the **transactions** table 
        into the **grouped_transactions** table.
        
        Transactions that share the same `group_id` are merged into a single grouped transaction. 
        This mechanism is used to consolidate split transactions (for example, payment requests).
        
        Each time this call is executed, the query will insert new grouped transactions 
        or overwrite existing records if necessary, ensuring the grouped data stays consistent 
        with the source table.
        """
    )

    @ApiResponse(responseCode = "200", description = "OK", content = @Content)
    @ApiResponse(responseCode = "500", description = "Internal server Error", content = @Content)
    @PostMapping("/etl")
    public ResponseEntity<Map<String, String>> runEtl() {
        groupedTransactionService.runGroupedTransactionsETL();
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

}