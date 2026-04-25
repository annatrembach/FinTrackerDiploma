package com.tracker.AIService.controllers;

import com.tracker.AIService.dto.ChatRequest;
import com.tracker.AIService.services.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request,
                       @RequestHeader("Authorization") String token) {
        return aiChatService.chat(request.getMessage(), token);
    }
}