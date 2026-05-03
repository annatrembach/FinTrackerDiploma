package com.tracker.AIService.models.dto.scheduledTransaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduledTransactionDraft {

    private String draftId;
    private Long userId;

    // Поля транзакції
    private Double amount;
    private String type;         // INCOME / EXPENSE
    private String category;
    private String description;

    // Специфічні для scheduled
    private String scheduledDate; // ISO: "2025-06-15T10:00:00"
    private String recurrence;    // ONCE / DAILY / WEEKLY / MONTHLY

    // Target — для UPDATE і CANCEL операцій
    private Long targetScheduledId;

    // Мета
    private Instant createdAt;

    // Прогрес заповнення (для AI діалогу)
    private String pendingField;
}