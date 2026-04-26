package com.tracker.AIService.tools;

import com.tracker.AIService.client.TransactionClient;
import com.tracker.AIService.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionTools {

    private final TransactionClient transactionClient;

    @Tool(description = """
        Get current balance, total income and total expenses for the user.
        Use when user asks: 'what is my balance', 'how much money do I have',
        'what are my total expenses or income'.
        """)
    public String getBalance() {
        return transactionClient.getBalance(
                AuthContext.getUserId(),
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get monthly income and expenses breakdown by month.
        Use when user asks: 'show my monthly statistics', 'how did I spend last month',
        'compare my income and expenses by month'.
        """)
    public String getMonthlySummary() {
        return transactionClient.getMonthlySummary(
                AuthContext.getUserId(),
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get full list of all transactions for the user.
        Use when user asks: 'show all my transactions', 'what did I spend money on',
        'list my recent purchases'.
        """)
    public String getUserTransactions() {
        return transactionClient.getUserTransactions(
                AuthContext.getUserId(),
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get filtered and sorted transactions.
        Use when user asks to filter by type (INCOME or EXPENSE),
        by category (e.g. 'food', 'transport'), or sort results.
        Pass null for parameters that are not mentioned by the user.
        """)
    public String getFilteredTransactions(String type, String category, String sortBy) {
        return transactionClient.getFilteredTransactions(
                type,
                category,
                sortBy,
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Create a new transaction for the user.
        Use when user says: 'add a transaction', 'I spent X on Y', 'record my income'.
        The transactionJson must be a valid JSON string with fields:
        amount (number), type ('INCOME' or 'EXPENSE'), category (string), description (string).
        Example: {"amount": 500, "type": "EXPENSE", "category": "food", "description": "lunch"}
        """)
    public String createTransaction(String transactionJson) {
        return transactionClient.createTransaction(
                transactionJson,
                AuthContext.getToken()
        );
    }
}