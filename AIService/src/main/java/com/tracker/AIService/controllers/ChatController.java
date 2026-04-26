package com.tracker.AIService.controllers;

import com.tracker.AIService.models.dto.ChatRequest;
import com.tracker.AIService.services.impls.AiChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final AiChatServiceImpl aiChatServiceImpl;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request,
                       @RequestHeader("Authorization") String token) {
        return aiChatServiceImpl.chat(request.getMessage(), token);
    }
}