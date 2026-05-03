package com.tracker.AIService.tools.scheduledTransaction;

import com.tracker.AIService.client.ScheduledTransactionClient;
import com.tracker.AIService.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTransactionQueryTools {

    private final ScheduledTransactionClient scheduledClient;

    @Tool(description = """
        Get all scheduled transactions for the current user.
        Use when user asks: 'show my scheduled transactions',
        'upcoming payments', 'recurring expenses', 'planned transactions'.
        """)
    public String getScheduledTransactions() {
        return scheduledClient.getByUser(
                AuthContext.getUserId(),
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get a specific scheduled transaction by its ID.
        Use when user asks: 'show scheduled transaction', 'get planned payment by id'.
        """)
    public String getScheduledTransactionById(Long id) {
        return scheduledClient.getById(
                id,
                AuthContext.getToken()
        );
    }
}