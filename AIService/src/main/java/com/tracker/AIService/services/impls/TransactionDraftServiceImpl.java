package com.tracker.AIService.services.impls;

import com.tracker.AIService.models.dto.transaction.TransactionDraft;
import com.tracker.AIService.services.TransactionDraftService;
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
public class TransactionDraftServiceImpl implements TransactionDraftService {

    private final RedisTemplate<String, TransactionDraft> transactionRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(15);

    @Override
    public String save(TransactionDraft draft) {
        String draftId = UUID.randomUUID().toString();

        draft.setDraftId(draftId);
        draft.setCreatedAt(Instant.now());

        String userKey = "txdraft:user:" + draft.getUserId();
        String draftKey = "txdraft:" + draftId;

        transactionRedisTemplate.opsForValue()
                .set(userKey, draft, TTL.toMinutes(), TimeUnit.MINUTES);

        transactionRedisTemplate.opsForValue()
                .set(draftKey, draft, TTL.toMinutes(), TimeUnit.MINUTES);

        return draftId;
    }

    @Override
    public Optional<TransactionDraft> getByUserId(Long userId) {
        return Optional.ofNullable(
                transactionRedisTemplate.opsForValue().get("txdraft:user:" + userId)
        );
    }

    @Override
    public void deleteByUserId(Long userId) {
        String userKey = "txdraft:user:" + userId;
        String draftId = String.valueOf(transactionRedisTemplate.opsForValue().get(userKey));
        if (draftId != null) {
            transactionRedisTemplate.delete("txdraft:" + draftId);
        }
        transactionRedisTemplate.delete(userKey);
    }
}
