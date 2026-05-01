package com.tracker.AIService.models.dto.analysis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResponse(
        String summary,
        List<TrendItem> trends,
        List<AnomalyItem> anomalies,
        GoalComparison goalComparison,
        List<String> savingTips,
        String overallHealthScore
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrendItem(
            String category,
            String direction,
            Double changePercent,
            String description
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AnomalyItem(
            String type,
            String description,
            Double amount,
            String date
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoalComparison(
            int totalGoals,
            int onTrackGoals,
            int atRiskGoals,
            String summary
    ) {}
}
