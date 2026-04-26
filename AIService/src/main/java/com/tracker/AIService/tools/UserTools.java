package com.tracker.AIService.tools;

import com.tracker.AIService.client.UserClient;
import com.tracker.AIService.dto.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTools {

    private final UserClient userClient;

    @Tool(description = """
            Get current user's profile details.
            Use when user asks: 'who am I', 'show my profile',
            'my account info', 'my details'.
            """)
    public String getMyProfile() {
        return userClient.getUserProfile(
                AuthContext.getToken()
        );
    }
}