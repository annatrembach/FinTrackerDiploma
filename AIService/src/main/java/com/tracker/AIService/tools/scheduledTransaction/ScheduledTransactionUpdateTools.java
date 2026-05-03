package com.tracker.AIService.tools.scheduledTransaction;

import com.tracker.AIService.client.ScheduledTransactionClient;
import com.tracker.AIService.models.dto.scheduledTransaction.ScheduledTransactionDraft;
import com.tracker.AIService.services.ScheduledTransactionDraftService;
import com.tracker.AIService.util.AuthContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScheduledTransactionUpdateTools {

    private final ScheduledTransactionClient scheduledClient;
    private final ScheduledTransactionDraftService draftService;
    private final ObjectMapper objectMapper;

    @Tool(description = """
        Step 1 of 2 for updating a scheduled transaction.
        Required: targetScheduledId (Long) — id of scheduled transaction to update.
        Optional: amount, type (INCOME/EXPENSE), scheduledDate (YYYY-MM-DDTHH:mm:ss),
                  recurrence (ONCE/DAILY/WEEKLY/MONTHLY), description, category.
        Saves update draft to Redis. Show changes to user and ask for confirmation.
        """)
    public ScheduledTransactionDraft prepareUpdateScheduled(
            Long targetScheduledId,
            Double amount,
            String type,
            String scheduledDate,
            String recurrence,
            String description,
            String category) {

        ScheduledTransactionDraft draft = new ScheduledTransactionDraft();
        draft.setUserId(AuthContext.getUserId());
        draft.setTargetScheduledId(targetScheduledId);
        draft.setAmount(amount);
        draft.setType(type);
        draft.setScheduledDate(scheduledDate);
        draft.setRecurrence(recurrence);
        draft.setDescription(description);
        draft.setCategory(category);

        draftService.save(draft);
        return draft;
    }

    @Tool(description = """
        Step 2 of 2 for updating a scheduled transaction.
        Call ONLY after explicit user confirmation.
        """)
    public String confirmUpdateScheduled() {
        Long userId = AuthContext.getUserId();

        ScheduledTransactionDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Update draft not found or expired"));

        if (draft.getTargetScheduledId() == null) {
            return "ERROR: targetScheduledId is missing in draft";
        }

        try {
            Map<String, Object> body = new HashMap<>();
            if (draft.getAmount() != null)        body.put("amount",        draft.getAmount());
            if (draft.getType() != null)          body.put("type",          draft.getType());
            if (draft.getScheduledDate() != null) body.put("scheduledDate", draft.getScheduledDate());
            if (draft.getRecurrence() != null)    body.put("recurrence",    draft.getRecurrence());
            if (draft.getDescription() != null)   body.put("description",   draft.getDescription());
            if (draft.getCategory() != null)      body.put("category",      Map.of("name", draft.getCategory()));

            String json = objectMapper.writeValueAsString(body);
            scheduledClient.update(draft.getTargetScheduledId(), json, AuthContext.getToken());
            draftService.deleteByUserId(userId);
            return "SCHEDULED_TRANSACTION_UPDATED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}