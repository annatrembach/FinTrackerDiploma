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
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoalUpdateTools {

    private final GoalClient goalClient;
    private final GoalDraftService draftService;
    private final GoalDraftValidator validator;
    private final ObjectMapper objectMapper;

    @Tool(description = """
        Step 1 of 2 for goal update.
        Saves update draft to Redis, returns draftId.
        Show data to user, ask for confirmation.
        Remember draftId — pass it to confirmUpdateGoal.
        """)
    public GoalDraftResponse prepareGoalUpdate(
            Long goalId,
            String title,
            Double targetAmount,
            String deadline) {

        GoalDraft draft = GoalDraft.builder()
                .userId(AuthContext.getUserId())
                .operation("UPDATE")
                .targetGoalId(goalId)
                .title(title)
                .targetAmount(targetAmount)
                .deadline(deadline)
                .build();

        String draftId = draftService.save(draft);

        return GoalDraftResponse.builder()
                .draftId(draftId)
                .status("DRAFT_READY")
                .operation("UPDATE")
                .targetGoalId(goalId)
                .title(title)
                .targetAmount(targetAmount)
                .deadline(deadline)
                .build();
    }

    @Tool(description = """
    Step 2 of 2 for goal update.
    Call ONLY after explicit user confirmation.
    """)
    public String confirmUpdateGoal() {

        Long userId = AuthContext.getUserId();

        GoalDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Draft not found or expired"));

        validator.validate(draft, userId);

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "name", draft.getTitle(),
                    "targetAmount", draft.getTargetAmount(),
                    "deadline", draft.getDeadline()
            ));

            goalClient.updateGoal(draft.getTargetGoalId(), json, AuthContext.getToken());

            draftService.deleteByUserId(userId);

            return "GOAL_UPDATED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Tool(description = """
    Step 1 of 2 for goal completion.
    Creates a completion draft and asks user for confirmation.
    Do NOT execute anything yet.
    """)
    public GoalDraftResponse prepareCompleteGoal(Long goalId) {

        GoalDraft draft = GoalDraft.builder()
                .userId(AuthContext.getUserId())
                .operation("COMPLETE")
                .targetGoalId(goalId)
                .build();

        String draftId = draftService.save(draft);

        return GoalDraftResponse.builder()
                .draftId(draftId)
                .status("DRAFT_READY")
                .operation("COMPLETE")
                .targetGoalId(goalId)
                .build();
    }

    @Tool(description = """
    Step 2 of 2 for goal completion.
    Call ONLY after explicit user confirmation.
    """)
    public String confirmCompleteGoal() {

        Long userId = AuthContext.getUserId();

        GoalDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Draft not found or expired"));

        validator.validate(draft, userId);

        try {
            goalClient.completeGoal(draft.getTargetGoalId(), AuthContext.getToken());

            draftService.deleteByUserId(userId);

            return "GOAL_COMPLETED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}