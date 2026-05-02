package com.tracker.AIService.services;

import com.tracker.AIService.models.dto.analysis.AnalysisRequest;
import com.tracker.AIService.models.dto.analysis.AnalysisResponse;

public interface AiAnalysisService {

    AnalysisResponse analyze(AnalysisRequest request, String token);
}
