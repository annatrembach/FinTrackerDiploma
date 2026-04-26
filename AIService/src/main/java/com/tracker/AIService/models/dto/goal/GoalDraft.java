package com.tracker.AIService.models.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDraft {
    private String draftId;
    private Long userId;
    private String operation;
    private Instant createdAt;

    private String title;
    private Double targetAmount;
    private Double currentAmount;
    private String deadline;

    private Long targetGoalId;
}