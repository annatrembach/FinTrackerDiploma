package com.tracker.AIService.tools;

import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalTools {

    private final GoalClient goalClient;

    @Tool(description = """
        Get all goals for the current user.
        Optionally filter by status (e.g. ACTIVE, COMPLETED).
        Use when user asks: 'show my goals', 'what are my goals',
        'list my active or completed goals'.
        """)
    public String getUserGoals(String status) {
        return goalClient.getUserGoals(
                UserContext.getUserId(),
                status,
                UserContext.getToken()
        );
    }

    @Tool(description = """
        Get a specific goal by its ID.
        Use when user asks: 'show goal details', 'get goal by id'.
        """)
    public String getGoalById(Long goalId) {
        return goalClient.getGoalById(
                goalId,
                UserContext.getToken()
        );
    }

    @Tool(description = """
        Create a new financial goal.
        Use when user says: 'create a goal', 'I want to save money for something'.
        The goalJson must be a valid JSON string with fields:
        title (string), targetAmount (number), currentAmount (number), deadline (date).
        Example: {"title": "Buy laptop", "targetAmount": 2000, "currentAmount": 200, "deadline": "2026-12-31"}
        """)
    public String createGoal(String goalJson) {
        return goalClient.createGoal(
                goalJson,
                UserContext.getToken()
        );
    }

    @Tool(description = """
        Update an existing goal.
        Use when user says: 'update my goal', 'change target amount', 'edit goal'.
        Provide goalId and updated goalJson.
        """)
    public String updateGoal(Long goalId, String goalJson) {
        return goalClient.updateGoal(
                goalId,
                goalJson,
                UserContext.getToken()
        );
    }

    @Tool(description = """
        Delete a goal.
        Use when user says: 'delete my goal', 'remove goal'.
        """)
    public String deleteGoal(Long goalId) {
        return goalClient.deleteGoal(
                goalId,
                UserContext.getToken()
        );
    }

    @Tool(description = """
        Mark a goal as completed.
        Use when user says: 'I completed my goal', 'mark goal as done'.
        """)
    public String completeGoal(Long goalId) {
        return goalClient.completeGoal(
                goalId,
                UserContext.getToken()
        );
    }
}
