package com.tracker.AIService.config;

import com.tracker.AIService.tools.GoalTools;
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
                                 GoalTools goalTools,
                                 UserTools userTools) {
        return ChatClient.builder(chatModel)
                .defaultTools(transactionTools, goalTools, userTools)
                .build();
    }
}
