package com.finsight.GroupedTransactionService.Service;

import com.finsight.GroupedTransactionService.Entity.GroupedTransactionDTO;
import com.finsight.GroupedTransactionService.Repository.GroupedTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupedTransactionService {

    @Autowired
    private GroupedTransactionRepository groupedTransactionRepository;
    private final JdbcTemplate jdbcTemplate;

    public GroupedTransactionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Run the ETL query: fill or update grouped_transactions based on transactions table
     */
    public int runGroupedTransactionsETL() {
        String sql = """
            INSERT INTO grouped_transactions (
                id, amount, category, date, description, recipient
            )
            SELECT 
                t.transactions_id AS id,
                totals.total_amount AS amount,
                t.category,
                t.date,
                t.description,
                t.recipient
            FROM transactions t
            JOIN (
                SELECT group_id, SUM(amount) AS total_amount
                FROM transactions
                GROUP BY group_id
            ) totals
              ON t.group_id = totals.group_id
            WHERE t.transactions_id = t.group_id
            ON CONFLICT (id) DO UPDATE SET
                amount = EXCLUDED.amount,
                category = EXCLUDED.category,
                date = EXCLUDED.date,
                description = EXCLUDED.description,
                recipient = EXCLUDED.recipient;
        """;

        return jdbcTemplate.update(sql);
    }
}
