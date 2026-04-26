package com.tracker.AIService.models.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDraft {
    private String draftId;
    private Long userId;
    private String operation;
    private Instant createdAt;

    private Double amount;
    private String type;            // INCOME | EXPENSE
    private String description;
    private String transactionDate;
    private Long categoryId;
    private Long goalId;

    private Long targetTransactionId;
}