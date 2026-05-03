package com.tracker.AIService.tools.scheduledTransaction;

import com.tracker.AIService.client.ScheduledTransactionClient;
import com.tracker.AIService.models.dto.scheduledTransaction.ScheduledTransactionDraft;
import com.tracker.AIService.services.ScheduledTransactionDraftService;
import com.tracker.AIService.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTransactionDeleteTools {

    private final ScheduledTransactionClient scheduledClient;
    private final ScheduledTransactionDraftService draftService;

    @Tool(description = """
        Step 1 of 2 for cancelling a scheduled transaction.
        Required: id of the scheduled transaction to cancel.
        Saves cancel draft to Redis. Show the id to user and ask for confirmation.
        """)
    public ScheduledTransactionDraft prepareCancelScheduled(Long targetScheduledId) {
        ScheduledTransactionDraft draft = new ScheduledTransactionDraft();
        draft.setUserId(AuthContext.getUserId());
        draft.setTargetScheduledId(targetScheduledId);

        draftService.save(draft);
        return draft;
    }

    @Tool(description = """
        Step 2 of 2 for cancelling a scheduled transaction.
        Call ONLY after explicit user confirmation.
        """)
    public String confirmCancelScheduled() {
        Long userId = AuthContext.getUserId();

        ScheduledTransactionDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cancel draft not found or expired"));

        if (draft.getTargetScheduledId() == null) {
            return "ERROR: targetScheduledId is missing in draft";
        }

        try {
            scheduledClient.cancel(draft.getTargetScheduledId(), AuthContext.getToken());
            draftService.deleteByUserId(userId);
            return "SCHEDULED_TRANSACTION_CANCELLED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}