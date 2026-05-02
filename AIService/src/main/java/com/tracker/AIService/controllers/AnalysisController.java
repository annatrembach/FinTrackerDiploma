package com.tracker.AIService.controllers;

import com.tracker.AIService.models.dto.analysis.AnalysisRequest;
import com.tracker.AIService.models.dto.analysis.AnalysisResponse;
import com.tracker.AIService.services.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping("/analyze")
    public AnalysisResponse analyze(@RequestBody AnalysisRequest request,
                                    @RequestHeader("Authorization") String token) {
        return aiAnalysisService.analyze(request, token);
    }
}