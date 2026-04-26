package com.tracker.AIService.tools.Goals;

import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.dto.AuthContext;
import com.tracker.AIService.dto.GoalContext;
import com.tracker.AIService.services.GoalDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalDeleteTools {

    private final GoalClient goalClient;
    private final GoalDraftService goalDraftService;

    @Tool(description = """
        Step 1 of 2 for goal deletion.
        Saves goalId to DB for confirmation step.
        After calling this, show the goalId to user and ask for explicit confirmation.
        Do NOT delete yet.
        Returns DELETE_DRAFT_READY on success.
        """)
    public String prepareDeleteGoal(Long goalId) {
        Long userId = AuthContext.getUserId();
        goalDraftService.saveDeleteId(userId, goalId);
        return "DELETE_DRAFT_READY. Goal id: " + goalId + ". Please ask user to confirm deletion.";
    }

    @Tool(description = """
        Step 2 of 2 for goal deletion.
        Call ONLY after user explicitly confirmed deletion.
        Loads goalId from DB and deletes goal.
        Returns GOAL_DELETED_SUCCESSFULLY on success.
        """)
    public String confirmDeleteGoal() {
        Long userId = AuthContext.getUserId();
        String token = AuthContext.getToken();

        Long goalId = goalDraftService.getDeleteId(userId);

        if (goalId == null) {
            return "ERROR: No delete draft found. Please start over.";
        }

        try {
            goalClient.deleteGoal(goalId, token);
            goalDraftService.clear(userId);
            return "GOAL_DELETED_SUCCESSFULLY";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}