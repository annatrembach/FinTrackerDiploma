package com.tracker.AIService.config;

import com.tracker.AIService.tools.doc.DocSearchTools;
import com.tracker.AIService.tools.goal.GoalQueryTools;
import com.tracker.AIService.tools.transaction.TransactionQueryTools;
import com.tracker.AIService.tools.user.UserQueryTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider financeMcpTools(GoalQueryTools goalQueryTools,
                                                TransactionQueryTools transactionQueryTools,
                                                UserQueryTools userQueryTools,
                                                DocSearchTools docSearchTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(goalQueryTools, transactionQueryTools,
                        userQueryTools, docSearchTools)
                .build();
    }
}