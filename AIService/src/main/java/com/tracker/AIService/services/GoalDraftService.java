package com.tracker.AIService.services;

import com.tracker.AIService.GoalDraftRepository;
import com.tracker.AIService.models.GoalDraft;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoalDraftService {

    private final GoalDraftRepository repository;

    public GoalDraft saveOrUpdate(Long userId, GoalDraft draft) {

        GoalDraft existing = repository.findByUserId(userId)
                .orElse(new GoalDraft());

        existing.setUserId(userId);
        existing.setTitle(draft.getTitle());
        existing.setTargetAmount(draft.getTargetAmount());
        existing.setCurrentAmount(draft.getCurrentAmount());
        existing.setDeadline(draft.getDeadline());
        existing.setStatus(draft.getStatus());

        return repository.save(existing);
    }

    public GoalDraft get(Long userId) {
        return repository.findByUserId(userId).orElse(null);
    }

    public void saveDeleteId(Long userId, Long goalId) {
        GoalDraft existing = repository.findByUserId(userId)
                .orElse(new GoalDraft());
        existing.setUserId(userId);
        existing.setDeleteGoalId(goalId);
        repository.save(existing);
    }

    public Long getDeleteId(Long userId) {
        return repository.findByUserId(userId)
                .map(GoalDraft::getDeleteGoalId)
                .orElse(null);
    }

    public void saveUpdateDraft(Long userId, Long goalId, String title, Double targetAmount, String deadline) {
        GoalDraft existing = repository.findByUserId(userId)
                .orElse(new GoalDraft());
        existing.setUserId(userId);
        existing.setUpdateGoalId(goalId);
        existing.setUpdateTitle(title);
        existing.setUpdateTargetAmount(targetAmount);
        existing.setUpdateDeadline(deadline);
        repository.save(existing);
    }

    public GoalDraft getUpdateDraft(Long userId) {
        return repository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public void clear(Long userId) {
        repository.deleteByUserId(userId);
    }
}
