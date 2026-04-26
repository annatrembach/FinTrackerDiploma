package com.tracker.AIService.services.impls;

import com.tracker.AIService.models.dto.goal.GoalDraft;
import com.tracker.AIService.services.GoalDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalDraftServiceImpl implements GoalDraftService {

    private final RedisTemplate<String, GoalDraft> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final String PREFIX = "draft:";

    @Override
    public String save(GoalDraft draft) {
        String draftId = UUID.randomUUID().toString();
        draft.setDraftId(draftId);
        draft.setCreatedAt(Instant.now());
        redisTemplate.opsForValue().set(PREFIX + draftId, draft, TTL);
        redisTemplate.opsForValue().set("draft:user:" + draft.getUserId(), draft, TTL);
        System.out.println("REDIS SAVE: " + PREFIX + draftId);
        return draftId;
    }

    @Override
    public Optional<GoalDraft> get(String draftId) {
        System.out.println("REDIS GET: " + PREFIX + draftId);
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(PREFIX + draftId)
        );
    }

    @Override
    public Optional<GoalDraft> getByUserId(Long userId) {
        System.out.println("REDIS GET BY USER: " + userId);

        return Optional.ofNullable(
                redisTemplate.opsForValue().get("draft:user:" + userId)
        );
    }

    @Override
    public void deleteByUserId(Long userId) {
        redisTemplate.delete("draft:user:" + userId);
    }

    @Override
    public void delete(String draftId) {
        redisTemplate.delete(PREFIX + draftId);
    }
}
