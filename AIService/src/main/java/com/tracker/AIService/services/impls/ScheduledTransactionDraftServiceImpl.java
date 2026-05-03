package com.tracker.AIService.services.impls;

import com.tracker.AIService.models.dto.scheduledTransaction.ScheduledTransactionDraft;
import com.tracker.AIService.services.ScheduledTransactionDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ScheduledTransactionDraftServiceImpl implements ScheduledTransactionDraftService {

    private final RedisTemplate<String, ScheduledTransactionDraft> scheduledDraftRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(20);

    private String userKey(Long userId) { return "schtxdraft:user:" + userId; }
    private String draftKey(String draftId) { return "schtxdraft:" + draftId; }

    @Override
    public String save(ScheduledTransactionDraft draft) {
        String draftId = UUID.randomUUID().toString();
        draft.setDraftId(draftId);
        draft.setCreatedAt(Instant.now());

        long ttlMinutes = TTL.toMinutes();

        scheduledDraftRedisTemplate.opsForValue()
                .set(userKey(draft.getUserId()), draft, ttlMinutes, TimeUnit.MINUTES);

        scheduledDraftRedisTemplate.opsForValue()
                .set(draftKey(draftId), draft, ttlMinutes, TimeUnit.MINUTES);

        return draftId;
    }

    @Override
    public Optional<ScheduledTransactionDraft> getByUserId(Long userId) {
        return Optional.ofNullable(
                scheduledDraftRedisTemplate.opsForValue().get(userKey(userId))
        );
    }

    @Override
    public void deleteByUserId(Long userId) {
        String uKey = userKey(userId);
        ScheduledTransactionDraft existing =
                scheduledDraftRedisTemplate.opsForValue().get(uKey);

        if (existing != null && existing.getDraftId() != null) {
            scheduledDraftRedisTemplate.delete(draftKey(existing.getDraftId()));
        }
        scheduledDraftRedisTemplate.delete(uKey);
    }

    /**
     * Якщо драфт вже є — мержимо лише non-null поля з patch.
     * Якщо немає — зберігаємо patch як новий драфт.
     */
    @Override
    public ScheduledTransactionDraft upsert(Long userId, ScheduledTransactionDraft patch) {
        ScheduledTransactionDraft existing = getByUserId(userId).orElse(new ScheduledTransactionDraft());
        existing.setUserId(userId);

        if (patch.getAmount() != null)        existing.setAmount(patch.getAmount());
        if (patch.getType() != null)          existing.setType(patch.getType());
        if (patch.getCategory() != null)      existing.setCategory(patch.getCategory());
        if (patch.getDescription() != null)   existing.setDescription(patch.getDescription());
        if (patch.getScheduledDate() != null) existing.setScheduledDate(patch.getScheduledDate());
        if (patch.getRecurrence() != null)    existing.setRecurrence(patch.getRecurrence());
        if (patch.getPendingField() != null)  existing.setPendingField(patch.getPendingField());

        save(existing);
        return existing;
    }

    /**
     * Драфт вважається повним якщо заповнені всі обов'язкові поля.
     */
    @Override
    public boolean isComplete(ScheduledTransactionDraft draft) {
        return draft.getAmount() != null
                && draft.getType() != null
                && draft.getScheduledDate() != null
                && draft.getRecurrence() != null
                && draft.getCategory() != null;
    }
}