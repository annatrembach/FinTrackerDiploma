package com.tracker.AIService.services;

import com.tracker.AIService.models.dto.goal.GoalDraft;
import com.tracker.AIService.models.dto.transaction.TransactionDraft;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Component
public class DraftValidator {

    private static final Set<String> VALID_OPS =
            Set.of("CREATE", "UPDATE", "DELETE", "COMPLETE");
    private static final Duration MAX_AGE = Duration.ofMinutes(15);

    public void validate(GoalDraft draft, Long requestingUserId) {
        validateCommon(draft.getUserId(), draft.getOperation(),
                draft.getCreatedAt(), requestingUserId);
    }

    public void validate(TransactionDraft draft, Long requestingUserId) {
        validateCommon(draft.getUserId(), draft.getOperation(),
                draft.getCreatedAt(), requestingUserId);
    }

    private void validateCommon(Long ownerId, String operation,
                                Instant createdAt, Long requestingUserId) {
        if (!ownerId.equals(requestingUserId)) {
            throw new SecurityException("Draft does not belong to this user");
        }
        if (!VALID_OPS.contains(operation)) {
            throw new IllegalStateException("Invalid operation: " + operation);
        }
        if (createdAt != null &&
                createdAt.isBefore(Instant.now().minus(MAX_AGE))) {
            throw new IllegalStateException("Draft expired");
        }
    }
}
