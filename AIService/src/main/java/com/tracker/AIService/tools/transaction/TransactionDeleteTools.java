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

@Component
@RequiredArgsConstructor
public class TransactionDeleteTools {

    private final TransactionClient transactionClient;
    private final TransactionDraftService draftService;
    private final DraftValidator validator;

    @Tool(description = """
        Step 1 of 2 for transaction deletion.
        Saves delete draft to Redis. Show transactionId to user and ask for confirmation.
        """)
    public TransactionDraftResponse prepareDeleteTransaction(Long transactionId) {

        TransactionDraft draft = TransactionDraft.builder()
                .userId(AuthContext.getUserId())
                .operation("DELETE")
                .targetTransactionId(transactionId)
                .build();

        String draftId = draftService.save(draft);

        return TransactionDraftResponse.builder()
                .draftId(draftId)
                .status("DRAFT_SAVED")
                .operation("DELETE")
                .targetTransactionId(transactionId)
                .build();
    }

    @Tool(description = """
        Step 2 of 2 for transaction deletion.
        Call ONLY after explicit user confirmation.
        """)
    public String confirmDeleteTransaction() {
        Long userId = AuthContext.getUserId();

        TransactionDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Draft not found or expired"));

        validator.validate(draft, userId);

        try {
            transactionClient.deleteTransaction(
                    draft.getTargetTransactionId(), AuthContext.getToken());
            draftService.deleteByUserId(userId);
            return "TRANSACTION_DELETED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}