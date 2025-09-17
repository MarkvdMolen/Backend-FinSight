package com.finsight.GroupedTransactionService.Controller;

import com.finsight.GroupedTransactionService.Service.GroupedTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/grouped-transactions")
@Tag(
        name = "Grouped Transactions",
        description = "ETL for Grouped Transactions"
)
public class GroupedTransactionController {

    @Autowired
    private GroupedTransactionService groupedTransactionService;

}