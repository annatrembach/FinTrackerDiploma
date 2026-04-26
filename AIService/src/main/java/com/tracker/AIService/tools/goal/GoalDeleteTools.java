package com.tracker.AIService.tools.goal;

import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.util.AuthContext;
import com.tracker.AIService.models.dto.GoalDraftResponse;
import com.tracker.AIService.models.dto.GoalDraft;
import com.tracker.AIService.services.GoalDraftService;
import com.tracker.AIService.services.GoalDraftValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalDeleteTools {

    private final GoalClient goalClient;
    private final GoalDraftService draftService;
    private final GoalDraftValidator validator;

    @Tool(description = """
        Step 1 of 2 for goal deletion.
        Saves delete draft to Redis, returns draftId.
        Show goalId to user, ask for explicit confirmation.
        Remember draftId — pass it to confirmDeleteGoal.
        """)
    public GoalDraftResponse prepareDeleteGoal(Long goalId) {

        GoalDraft draft = GoalDraft.builder()
                .userId(AuthContext.getUserId())
                .operation("DELETE")
                .targetGoalId(goalId)
                .build();

        String draftId = draftService.save(draft);

        return GoalDraftResponse.builder()
                .draftId(draftId)
                .status("DRAFT_READY")
                .operation("DELETE")
                .targetGoalId(goalId)
                .build();
    }

    @Tool(description = """
    Step 2 of 2 for goal deletion.
    Call ONLY after explicit user confirmation.
    """)
    public String confirmDeleteGoal() {

        Long userId = AuthContext.getUserId();

        GoalDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Draft not found or expired"));

        validator.validate(draft, userId);

        try {
            goalClient.deleteGoal(draft.getTargetGoalId(), AuthContext.getToken());

            draftService.deleteByUserId(userId);

            return "GOAL_DELETED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}