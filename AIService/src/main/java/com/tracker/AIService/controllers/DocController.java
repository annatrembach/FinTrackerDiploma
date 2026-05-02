package com.tracker.AIService.controllers;

import com.tracker.AIService.models.dto.doc.DocRequest;
import com.tracker.AIService.services.DocumentIngestionService;
import com.tracker.AIService.services.AiDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class DocController {

    private final AiDocService aiDocService;
    private final DocumentIngestionService documentIngestionService;

    @PostMapping("/doc")
    public ResponseEntity<String> askDoc(@RequestBody DocRequest request,
                                         @RequestHeader("Authorization") String token) {
        String answer = aiDocService.answer(request, token);
        return ResponseEntity.ok(answer);
    }

    @PostMapping("/doc/reingest")
    public ResponseEntity<String> reIngest() {
        documentIngestionService.reIngestDocuments();
        return ResponseEntity.ok("Documentation re-ingested successfully");
    }
}