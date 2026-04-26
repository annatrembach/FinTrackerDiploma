package com.tracker.AIService;

import com.tracker.AIService.models.GoalDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoalDraftRepository extends JpaRepository<GoalDraft, Long> {

    Optional<GoalDraft> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}