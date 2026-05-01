package com.tracker.AIService.services.impls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.AIService.client.GoalClient;
import com.tracker.AIService.client.TransactionClient;
import com.tracker.AIService.models.dto.analysis.AnalysisRequest;
import com.tracker.AIService.models.dto.analysis.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl {

    private final TransactionClient transactionClient;
    private final GoalClient goalClient;
    private final ObjectMapper objectMapper;

    @Qualifier("analysisChatClient")
    private final ChatClient analysisChatClient;

    @Value("${app.analysis.max-transactions:200}")
    private int maxTransactions;

    public AnalysisResponse analyze(AnalysisRequest request, String token) {
        log.info("Starting analysis for userId={}, period={}", request.userId(), request.period());

        String transactionsJson  = fetchTransactions(request.userId(), token);
        String monthlySummaryJson = fetchMonthlySummary(request.userId(), token);
        String balanceJson        = fetchBalance(request.userId(), token);
        String goalsJson          = fetchGoals(request.userId(), token);

        String prompt = buildAnalysisPrompt(request, transactionsJson, monthlySummaryJson, balanceJson, goalsJson);

        String rawResponse = analysisChatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseResponse(rawResponse);
    }

    private String fetchTransactions(Long userId, String token) {
        try {
            return transactionClient.getUserTransactions(userId, token);
        } catch (Exception e) {
            log.warn("Failed to fetch transactions for userId={}: {}", userId, e.getMessage());
            return "[]";
        }
    }

    private String fetchMonthlySummary(Long userId, String token) {
        try {
            return transactionClient.getMonthlySummary(userId, token);
        } catch (Exception e) {
            log.warn("Failed to fetch monthly summary for userId={}: {}", userId, e.getMessage());
            return "{}";
        }
    }

    private String fetchBalance(Long userId, String token) {
        try {
            return transactionClient.getUserBalance(userId, token);
        } catch (Exception e) {
            log.warn("Failed to fetch balance for userId={}: {}", userId, e.getMessage());
            return "{}";
        }
    }

    private String fetchGoals(Long userId, String token) {
        try {
            return goalClient.getUserGoals(userId, null, token);
        } catch (Exception e) {
            log.warn("Failed to fetch goals for userId={}: {}", userId, e.getMessage());
            return "[]";
        }
    }

    private String buildAnalysisPrompt(AnalysisRequest request,
                                       String transactions,
                                       String monthlySummary,
                                       String balance,
                                       String goals) {
        return """
                Analyze the following financial data for period: %s.
                
                ## Current Balance
                %s
                
                ## Monthly Summary
                %s
                
                ## Transactions (last %d max)
                %s
                
                ## Financial Goals
                %s
                
                Return a JSON object with this exact structure:
                {
                  "summary": "2-3 sentence overview of financial health",
                  "trends": [
                    {
                      "category": "category name",
                      "direction": "UP|DOWN|STABLE",
                      "changePercent": 15.5,
                      "description": "brief explanation"
                    }
                  ],
                  "anomalies": [
                    {
                      "type": "SPIKE|UNUSUAL_CATEGORY|BUDGET_BREACH",
                      "description": "what happened",
                      "amount": 250.00,
                      "date": "2024-01-15"
                    }
                  ],
                  "goalComparison": {
                    "totalGoals": 3,
                    "onTrackGoals": 2,
                    "atRiskGoals": 1,
                    "summary": "brief assessment"
                  },
                  "savingTips": [
                    "Specific tip based on data"
                  ],
                  "overallHealthScore": "GOOD|MODERATE|NEEDS_ATTENTION"
                }
                """.formatted(
                request.period(),
                balance,
                monthlySummary,
                maxTransactions,
                truncateIfNeeded(transactions),
                goals
        );
    }

    private String truncateIfNeeded(String json) {
        if (json == null) return "[]";
        int limit = maxTransactions * 250;
        if (json.length() > limit) {
            log.warn("Transactions JSON too large ({}), truncating to {} chars", json.length(), limit);
            return json.substring(0, limit) + "... (truncated)]";
        }
        return json;
    }

    private AnalysisResponse parseResponse(String rawResponse) {
        try {
            String cleaned = rawResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();
            return objectMapper.readValue(cleaned, AnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse Claude analysis response: {}", e.getMessage());
            log.debug("Raw response was: {}", rawResponse);
            return new AnalysisResponse(
                    "Analysis completed but response parsing failed. Raw: " + rawResponse.substring(0, Math.min(200, rawResponse.length())),
                    java.util.List.of(),
                    java.util.List.of(),
                    new AnalysisResponse.GoalComparison(0, 0, 0, "unavailable"),
                    java.util.List.of(),
                    "MODERATE"
            );
        }
    }
}