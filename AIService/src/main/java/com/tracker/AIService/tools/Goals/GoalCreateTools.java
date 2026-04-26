package com.tracker.AIService.tools.Goals;

import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.dto.AuthContext;
import com.tracker.AIService.models.GoalDraft;
import com.tracker.AIService.services.GoalDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalCreateTools {

    private final GoalClient goalClient;

    private final GoalDraftService goalDraftService;

    @Tool(description = """
    Step 1 of 2 for goal creation.
    Call this FIRST to collect and save goal data as a draft.
    Required parameters: title (String), targetAmount (Double),
    currentAmount (Double), deadline (String, format: YYYY-MM-DD).
    After calling this, show the collected data to the user
    and ask for explicit confirmation before proceeding.
    Returns DRAFT_SAVED on success.
    """)
    public String prepareGoal(String title,
                              Double targetAmount,
                              Double currentAmount,
                              String deadline) {

        Long userId = AuthContext.getUserId();

        GoalDraft draft = new GoalDraft();
        draft.setUserId(userId);
        draft.setTitle(title);
        draft.setTargetAmount(targetAmount);
        draft.setCurrentAmount(currentAmount);
        draft.setStatus("IN_PROGRESS");
        draft.setDeadline(deadline);

        goalDraftService.saveOrUpdate(userId, draft);

        return """
        DRAFT_SAVED. Please show this to user and ask for confirmation:
        - Title: %s
        - Target Amount: %s
        - Current Amount: %s
        - Deadline: %s
        """.formatted(title, targetAmount, currentAmount, deadline);
    }

    @Tool(description = """
    Step 2 of 2 for goal creation.
    Call this ONLY after user has explicitly confirmed they want to save the goal.
    Do NOT call this automatically - wait for user approval.
    Loads the draft from DB and sends it to GoalService.
    Returns the created goal JSON on success.
    When a tool returns "GOAL_CREATED_SUCCESSFULLY", respond to the user with a friendly confirmation message in their language.
    When a tool returns text starting with "ERROR:", explain the error to the user in simple terms.
    Never say "technical error" when a goal was successfully created.
    """)
    public String confirmCreateGoal() {
        Long userId = AuthContext.getUserId();
        String token = AuthContext.getToken();

        GoalDraft draft = goalDraftService.get(userId);
        if (draft == null) {
            return "ERROR: No draft found. Please start over.";
        }

        String json = """
        {
          "name": "%s",
          "targetAmount": %s,
          "currentAmount": %s,
          "deadline": "%s",
          "userId": %s
        }
        """.formatted(
                draft.getTitle(),
                draft.getTargetAmount(),
                draft.getCurrentAmount(),
                draft.getDeadline(),
                userId
        );

        try {
            String result = goalClient.createGoal(json, token);
            goalDraftService.clear(userId);
            return "GOAL_CREATED_SUCCESSFULLY";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
