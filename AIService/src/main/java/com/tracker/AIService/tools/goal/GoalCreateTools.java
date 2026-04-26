package com.tracker.AIService.tools.goal;

import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.util.AuthContext;
import com.tracker.AIService.models.dto.goal.GoalDraftResponse;
import com.tracker.AIService.models.dto.goal.GoalDraft;
import com.tracker.AIService.services.GoalDraftService;
import com.tracker.AIService.services.DraftValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoalCreateTools {

    private final GoalClient goalClient;
    private final GoalDraftService draftService;
    private final DraftValidator validator;
    private final ObjectMapper objectMapper;

    @Tool(description = """
    Step 1 of 2 for goal creation.
    Saves draft to Redis, returns draftId.
            IMPORTANT:
            - You MUST store draftId from response
            - You MUST pass draftId into confirmCreateGoal
            - NEVER ask user for draftId
            - Always reuse it automatically
    """)
    public GoalDraftResponse prepareGoal(
            String title,
            Double targetAmount,
            Double currentAmount,
            String deadline) {

        GoalDraft draft = GoalDraft.builder()
                .userId(AuthContext.getUserId())
                .operation("CREATE")
                .title(title)
                .targetAmount(targetAmount)
                .currentAmount(currentAmount)
                .deadline(deadline)
                .build();

        String draftId = draftService.save(draft);

        return GoalDraftResponse.builder()
                .draftId(draftId)
                .status("DRAFT_SAVED")
                .operation("CREATE")
                .title(title)
                .targetAmount(targetAmount)
                .currentAmount(currentAmount)
                .deadline(deadline)
                .build();
    }

    @Tool(description = """
        Step 2 of 2 for goal creation.
        Call ONLY after explicit user confirmation.
        draftId — value from prepareGoal response.
        """)
    public String confirmCreateGoal() {
        Long userId = AuthContext.getUserId();

        GoalDraft draft = draftService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Draft not found or expired"));

        validator.validate(draft, userId);

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "name", draft.getTitle(),
                    "targetAmount", draft.getTargetAmount(),
                    "currentAmount", draft.getCurrentAmount(),
                    "deadline", draft.getDeadline(),
                    "userId", userId
            ));

            goalClient.createGoal(json, AuthContext.getToken());

            draftService.deleteByUserId(userId);

            return "GOAL_CREATED_SUCCESSFULLY";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
