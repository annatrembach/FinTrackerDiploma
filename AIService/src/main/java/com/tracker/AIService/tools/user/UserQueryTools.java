package com.tracker.AIService.tools.user;

import com.tracker.AIService.client.UserClient;
import com.tracker.AIService.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserQueryTools {

    private final UserClient userClient;

    @Tool(description = """
        Get current user profile (name, email, role).
        Use when user asks: 'my profile', 'who am I', 'show my account', 'my info'.
        """)
    public String getMyProfile() {
        return userClient.getUserProfile(AuthContext.getToken());
    }

}
