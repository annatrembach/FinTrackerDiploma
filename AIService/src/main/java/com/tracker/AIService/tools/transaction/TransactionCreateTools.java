package com.tracker.AIService.tools.transaction;

import com.tracker.AIService.client.TransactionClient;
import com.tracker.AIService.models.dto.transaction.TransactionDraft;
import com.tracker.AIService.models.dto.transaction.TransactionDraftResponse;
import com.tracker.AIService.services.DraftValidator;
import com.tracker.AIService.services.TransactionDraftService;
import com.tracker.AIService.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransactionCreateTools {

    private final TransactionClient transactionClient;
    private final TransactionDraftService draftService;
    private final DraftValidator validator;
    private final ObjectMapper objectMapper;

    @Tool(description = """
        Step 1 of 2 for transaction creation.
        Required: amount (Double), type (INCOME or EXPENSE), transactionDate (YYYY-MM-DD).
        Optional: description, categoryId, goalId.
        Saves draft to Redis. Show data to user and ask for confirmation.
        """)
    public TransactionDraftResponse prepareTransaction(
            Double amount,
            String type,
            String description,
            String transactionDate,
            Long categoryId,
            Long goalId) {

        TransactionDraft draft = TransactionDraft.builder()
                .userId(AuthContext.getUserId())
                .operation("CREATE")
                .amount(amount)
                .type(type)
                .description(description)
                .transactionDate(transactionDate)
                .categoryId(categoryId)
                .goalId(goalId)
                .build();

        String draftId = draftService.save(draft);

        return TransactionDraftResponse.builder()
                .draftId(draftId)
                .status("DRAFT_SAVED")
                .operation("CREATE")
                .amount(amount)
                .type(type)
                .description(description)
                .transactionDate(transactionDate)
                .categoryId(categoryId)
                .goalId(goalId)
                .build();
    }

    @Tool(description = """
        Step 2 of 2 for transaction creation.
        Call ONLY after explicit user confirmation.
        """)
    public String confirmCreateTransaction() {
        Long userId = AuthContext.getUserId();

        TransactionDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Draft not found or expired"));

        validator.validate(draft, userId);

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "amount",          draft.getAmount(),
                    "type",            draft.getType(),
                    "description",     draft.getDescription() != null ? draft.getDescription() : "",
                    "transactionDate", draft.getTransactionDate(),
                    "userId",          userId,
                    "categoryId",      draft.getCategoryId() != null ? draft.getCategoryId() : 0,
                    "goalId",          draft.getGoalId()     != null ? draft.getGoalId()     : 0
            ));

            transactionClient.createTransaction(json, AuthContext.getToken());
            draftService.deleteByUserId(userId);
            return "TRANSACTION_CREATED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
