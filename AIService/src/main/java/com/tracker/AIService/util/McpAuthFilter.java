package com.tracker.AIService.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class McpAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        log.info("MCP {} {} authHeader={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader(HttpHeaders.AUTHORIZATION) == null ? "ABSENT" : "present");

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token == null || token.isBlank()) {
            unauthorized(response, "Missing Authorization header");
            return;
        }

        try {
            Long userId = jwtUtil.extractUserId(token);
            AuthContext.setUserId(userId);
            AuthContext.setToken(token);
        } catch (Exception e) {
            unauthorized(response, "Invalid token");
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            AuthContext.clearUserAuthData();
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}