package com.tracker.AIService.services;

import com.tracker.AIService.config.JwtUtil;
import com.tracker.AIService.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AiChatService {

    private final ChatClient chatClient;
    private final JwtUtil jwtUtil;

    public String chat(String message, String token) {

        Long userId = jwtUtil.extractUserId(token);

        UserContext.setUserId(userId);
        UserContext.setToken(token);

        return chatClient.prompt()
                .system("""
        You are a personal financial assistant.

        Rules:
        - NEVER ask the user for their userId — you already have it
        - ALWAYS use the available tools to fetch real data
        - When the user asks about balance or spending — use transaction tools
        - When the user asks about goals or savings — use goal tools
        - When the user asks about their profile or account — use user tools
        - Respond in the same language the user writes in
        - Respond in plain text without markdown formatting.
        - Be concise and helpful
    """)
                .user(message)
                .call()
                .content();
    }
}
