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
public class TransactionUpdateTools {

    private final TransactionClient transactionClient;
    private final TransactionDraftService draftService;
    private final DraftValidator validator;
    private final ObjectMapper objectMapper;

    @Tool(description = """
        Step 1 of 2 for transaction update.
        Required: transactionId. Optional: amount, type, description, transactionDate, categoryId.
        Saves update draft to Redis. Show data to user and ask for confirmation.
        """)
    public TransactionDraftResponse prepareUpdateTransaction(
            Long transactionId,
            Double amount,
            String type,
            String description,
            String transactionDate,
            Long categoryId) {

        TransactionDraft draft = TransactionDraft.builder()
                .userId(AuthContext.getUserId())
                .operation("UPDATE")
                .targetTransactionId(transactionId)
                .amount(amount)
                .type(type)
                .description(description)
                .transactionDate(transactionDate)
                .categoryId(categoryId)
                .build();

        String draftId = draftService.save(draft);

        return TransactionDraftResponse.builder()
                .draftId(draftId)
                .status("DRAFT_SAVED")
                .operation("UPDATE")
                .targetTransactionId(transactionId)
                .amount(amount)
                .type(type)
                .description(description)
                .transactionDate(transactionDate)
                .categoryId(categoryId)
                .build();
    }

    @Tool(description = """
        Step 2 of 2 for transaction update.
        Call ONLY after explicit user confirmation.
        """)
    public String confirmUpdateTransaction() {
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
                    "categoryId",      draft.getCategoryId() != null ? draft.getCategoryId() : 0
            ));

            transactionClient.updateTransaction(
                    draft.getTargetTransactionId(), json, AuthContext.getToken());
            draftService.deleteByUserId(userId);
            return "TRANSACTION_UPDATED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}