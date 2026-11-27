package com.tracker.AIService.config;

import com.tracker.AIService.util.JwtUtil;
import com.tracker.AIService.util.McpAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpSecurityConfig {

    @Bean
    public FilterRegistrationBean<McpAuthFilter> mcpAuthFilter(JwtUtil jwtUtil) {
        FilterRegistrationBean<McpAuthFilter> reg =
                new FilterRegistrationBean<>(new McpAuthFilter(jwtUtil));
        reg.addUrlPatterns("/mcp", "/mcp/*");
        reg.setOrder(1);
        return reg;
    }
}