package com.tracker.AIService.config;

import com.tracker.AIService.tools.Goals.GoalCreateTools;
import com.tracker.AIService.tools.Goals.GoalDeleteTools;
import com.tracker.AIService.tools.Goals.GoalQueryTools;
import com.tracker.AIService.tools.Goals.GoalUpdateTools;
import com.tracker.AIService.tools.TransactionTools;
import com.tracker.AIService.tools.UserTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel,
                                 TransactionTools transactionTools,
                                 GoalCreateTools goalCreateTools,
                                 GoalDeleteTools goalDeleteTools,
                                 GoalUpdateTools goalUpdateTools,
                                 GoalQueryTools goalQueryTools,
                                 UserTools userTools) {
        return ChatClient.builder(chatModel)
                .defaultTools(transactionTools, goalCreateTools, goalDeleteTools, goalUpdateTools, goalQueryTools, userTools)
                .build();
    }
}
