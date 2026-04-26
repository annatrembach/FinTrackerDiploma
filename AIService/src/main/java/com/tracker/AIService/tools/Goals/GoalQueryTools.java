package com.tracker.AIService.tools.Goals;

import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.dto.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalQueryTools {

    private final GoalClient goalClient;

    @Tool(description = """
        Get all goals for the current user.
        Optionally filter by status (e.g. ACTIVE, COMPLETED).
        Use when user asks: 'show my goals', 'what are my goals',
        'list my active or completed goals'.
        """)
    public String getUserGoals(String status) {
        return goalClient.getUserGoals(
                AuthContext.getUserId(),
                status,
                AuthContext.getToken()
        );
    }

    @Tool(description = """
        Get a specific goal by its ID.
        Use when user asks: 'show goal details', 'get goal by id'.
        """)
    public String getGoalById(Long goalId) {
        return goalClient.getGoalById(
                goalId,
                AuthContext.getToken()
        );
    }
}
