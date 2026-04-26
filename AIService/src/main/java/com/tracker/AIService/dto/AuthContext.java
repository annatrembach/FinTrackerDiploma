package com.tracker.AIService.dto;

import java.util.HashMap;
import java.util.Map;

public class AuthContext {

    private static final ThreadLocal<Map<String, Object>> context =
            ThreadLocal.withInitial(HashMap::new);

    public static void setUserId(Long userId) {
        context.get().put("userId", userId);
    }

    public static Long getUserId() {
        return (Long) context.get().get("userId");
    }

    public static void setToken(String token) {
        context.get().put("token", token);
    }

    public static String getToken() {
        return (String) context.get().get("token");
    }

    public static void clearUserAuthData() {
        context.get().remove("userId");
        context.get().remove("token");
    }

}