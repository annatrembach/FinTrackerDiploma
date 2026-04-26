package com.tracker.AIService.tools.Goals;

import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.dto.AuthContext;
import com.tracker.AIService.dto.GoalContext;
import com.tracker.AIService.dto.GoalDraftDTO;
import com.tracker.AIService.models.GoalDraft;
import com.tracker.AIService.services.GoalDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalUpdateTools {

    private final GoalClient goalClient;
    private final GoalDraftService goalDraftService;

    @Tool(description = """
        Step 1 of 2 for goal update.
        Saves update draft to DB.
        After calling this, show the data to user and ask for explicit confirmation.
        Do NOT update yet.
        Returns UPDATE_DRAFT_READY on success.
        """)
    public String prepareGoalUpdate(Long goalId, String title, Double targetAmount, String deadline) {
        Long userId = AuthContext.getUserId();

        goalDraftService.saveUpdateDraft(userId, goalId, title, targetAmount, deadline);

        return """
            UPDATE_DRAFT_READY. Please show to user and ask for confirmation:
            - Goal ID: %s
            - Title: %s
            - Target Amount: %s
            - Deadline: %s
            """.formatted(goalId, title, targetAmount, deadline);
    }

    @Tool(description = """
        Step 2 of 2 for goal update.
        Call ONLY after user explicitly confirmed.
        Loads update draft from DB and sends PUT to GoalService.
        Returns GOAL_UPDATED_SUCCESSFULLY on success.
        """)
    public String confirmUpdateGoal() {
        Long userId = AuthContext.getUserId();
        String token = AuthContext.getToken();

        GoalDraft draft = goalDraftService.getUpdateDraft(userId);

        if (draft == null || draft.getUpdateGoalId() == null) {
            return "ERROR: No update draft found. Please start over.";
        }

        String json = """
            {
                "name": "%s",
                "targetAmount": %s,
                "deadline": "%s"
            }
            """.formatted(
                draft.getUpdateTitle(),
                draft.getUpdateTargetAmount(),
                draft.getUpdateDeadline()
        );

        try {
            goalClient.updateGoal(draft.getUpdateGoalId(), json, token);
            goalDraftService.clear(userId);
            return "GOAL_UPDATED_SUCCESSFULLY";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}