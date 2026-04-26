package com.tracker.AIService.services;

import com.tracker.AIService.models.dto.GoalDraft;

import java.util.Optional;

public interface GoalDraftService {

    String save(GoalDraft draft);

    Optional<GoalDraft> get(String draftId);

    Optional<GoalDraft> getByUserId(Long userId);

    void deleteByUserId(Long userId);

    void delete(String draftId);

}
