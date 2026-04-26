package com.tracker.AIService.services;

import com.tracker.AIService.models.dto.GoalDraft;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Component
public class GoalDraftValidator {

    private static final Set<String> VALID_OPS =
            Set.of("CREATE", "UPDATE", "DELETE");

    public void validate(GoalDraft draft, Long requestingUserId) {
        if (!draft.getUserId().equals(requestingUserId)) {
            throw new SecurityException("Draft does not belong to this user");
        }
        if (!VALID_OPS.contains(draft.getOperation())) {
            throw new IllegalStateException("Invalid operation: " + draft.getOperation());
        }
        if (draft.getCreatedAt().isBefore(Instant.now().minus(Duration.ofMinutes(15)))) {
            throw new IllegalStateException("Draft expired");
        }
    }
}
