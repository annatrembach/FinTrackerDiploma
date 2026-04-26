package com.tracker.AIService.services;

import com.tracker.AIService.models.dto.transaction.TransactionDraft;

import java.util.Optional;

public interface TransactionDraftService {
    String save(TransactionDraft draft);
    Optional<TransactionDraft> getByUserId(Long userId);
    void deleteByUserId(Long userId);
}
