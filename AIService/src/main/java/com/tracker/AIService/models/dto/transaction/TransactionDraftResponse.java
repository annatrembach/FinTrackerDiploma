package com.tracker.AIService.models.dto.transaction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDraftResponse {
    private String draftId;
    private String status;
    private String operation;

    private Double amount;
    private String type;
    private String description;
    private String transactionDate;
    private Long categoryId;
    private Long goalId;
    private Long targetTransactionId;
}