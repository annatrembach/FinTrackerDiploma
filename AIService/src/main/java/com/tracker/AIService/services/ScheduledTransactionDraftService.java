package com.tracker.AIService.services;

import com.tracker.AIService.models.dto.scheduledTransaction.ScheduledTransactionDraft;

import java.util.Optional;

public interface ScheduledTransactionDraftService {

    String save(ScheduledTransactionDraft draft);

    Optional<ScheduledTransactionDraft> getByUserId(Long userId);

    void deleteByUserId(Long userId);

    /** Оновлює існуючий драфт або створює новий якщо не існує */
    ScheduledTransactionDraft upsert(Long userId, ScheduledTransactionDraft patch);

    boolean isComplete(ScheduledTransactionDraft draft);
}