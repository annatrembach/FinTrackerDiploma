package com.tracker.AIService.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "goal_draft")
public class GoalDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String title;
    private Double targetAmount;
    private Double currentAmount;
    private String status;
    private String deadline;

    // delete draft
    private Long deleteGoalId;

    // update draft
    private Long updateGoalId;
    private String updateTitle;
    private Double updateTargetAmount;
    private String updateDeadline;
}