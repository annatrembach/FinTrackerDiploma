package com.tracker.AIService.models.dto.analysis;

public record AnalysisRequest(
        Long userId,
        String period
) {}