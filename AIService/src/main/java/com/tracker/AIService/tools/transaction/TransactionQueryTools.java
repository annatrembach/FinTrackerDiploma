package com.tracker.AIService.tools.transaction;

import com.tracker.AIService.client.TransactionClient;
import com.tracker.AIService.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionQueryTools {

    private final TransactionClient transactionClient;

    @Tool(description = """
        Get all transactions for the current user.
        Use when user asks: 'show my transactions', 'my history', 'what did I spend'.
        """)
    public String getUserTransactions() {
        return transactionClient.getUserTransactions(
                AuthContext.getUserId(),
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get a specific transaction by its ID.
        Use when user asks: 'show transaction', 'get transaction by id'.
        """)
    public String getTransactionById(Long transactionId) {
        return transactionClient.getTransactionById(
                transactionId,
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Filter transactions by type (INCOME/EXPENSE), category, or sort order.
        Use when user asks: 'show my expenses', 'income this month', 'sort by date'.
        """)
    public String getFilteredTransactions(String type, String category, String sortBy) {
        return transactionClient.getFilteredTransactions(
                type, category, sortBy,
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get current balance, total income and expenses for the user.
        Use when user asks: 'my balance', 'how much do I have', 'total income'.
        """)
    public String getUserBalance() {
        return transactionClient.getUserBalance(
                AuthContext.getUserId(),
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get monthly income and expenses summary.
        Use when user asks: 'monthly summary', 'this month stats', 'spending by month'.
        """)
    public String getMonthlySummary() {
        return transactionClient.getMonthlySummary(
                AuthContext.getUserId(),
                AuthContext.getToken()
        );
    }
}