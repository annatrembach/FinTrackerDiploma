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
public class ScheduledTransactionCreateTools {

    private final ScheduledTransactionClient scheduledClient;
    private final ScheduledTransactionDraftService draftService;
    private final ObjectMapper objectMapper;

    @Tool(description = """
        Step 1 of 2 for scheduled transaction creation.
        Required: amount (Double), type (INCOME or EXPENSE),
                  scheduledDate (ISO: YYYY-MM-DDTHH:mm:ss),
                  recurrence (ONCE / DAILY / WEEKLY / MONTHLY).
        Optional: description, category.
        Saves draft to Redis. Show data to user and ask for confirmation.
        """)
    public ScheduledTransactionDraft prepareScheduledTransaction(
            Double amount,
            String type,
            String scheduledDate,
            String recurrence,
            String description,
            String category) {

        ScheduledTransactionDraft draft = new ScheduledTransactionDraft();
        draft.setUserId(AuthContext.getUserId());
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
        Step 2 of 2 for scheduled transaction creation.
        Call ONLY after explicit user confirmation.
        Reads draft from Redis and sends it to TransactionService.
        """)
    public String confirmScheduledTransaction() {
        Long userId = AuthContext.getUserId();

        ScheduledTransactionDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Scheduled draft not found or expired"));

        if (!draftService.isComplete(draft)) {
            return "DRAFT_INCOMPLETE: " + missingFields(draft);
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("amount",        draft.getAmount());
            body.put("type",          draft.getType());
            body.put("scheduledDate", draft.getScheduledDate());
            body.put("recurrence",    draft.getRecurrence());
            body.put("description",   draft.getDescription() != null ? draft.getDescription() : "");
            body.put("userId",        userId);
            body.put("category",      Map.of("name", draft.getCategory() != null ? draft.getCategory() : ""));

            String json = objectMapper.writeValueAsString(body);
            scheduledClient.createScheduled(json, AuthContext.getToken());
            draftService.deleteByUserId(userId);
            return "SCHEDULED_TRANSACTION_CREATED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String missingFields(ScheduledTransactionDraft d) {
        StringBuilder sb = new StringBuilder();
        if (d.getAmount() == null)        sb.append("amount ");
        if (d.getType() == null)          sb.append("type ");
        if (d.getScheduledDate() == null) sb.append("scheduledDate ");
        if (d.getRecurrence() == null)    sb.append("recurrence ");
        if (d.getCategory() == null)      sb.append("category ");
        return sb.toString().trim();
    }
}