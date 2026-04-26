package com.tracker.AIService.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoalDraftResponse {
    private String draftId;
    private String status;
    private String operation;

    private String title;
    private Double targetAmount;
    private Double currentAmount;
    private String deadline;
    private Long targetGoalId;
}
