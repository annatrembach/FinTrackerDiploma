package com.tracker.AIService.config;

import com.tracker.AIService.tools.goal.GoalCreateTools;
import com.tracker.AIService.tools.goal.GoalDeleteTools;
import com.tracker.AIService.tools.goal.GoalQueryTools;
import com.tracker.AIService.tools.goal.GoalUpdateTools;
import com.tracker.AIService.tools.transaction.TransactionCreateTools;
import com.tracker.AIService.tools.transaction.TransactionDeleteTools;
import com.tracker.AIService.tools.transaction.TransactionQueryTools;
import com.tracker.AIService.tools.transaction.TransactionUpdateTools;
import com.tracker.AIService.tools.user.UserQueryTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel,
                                 TransactionCreateTools transactionCreateTools,
                                 TransactionDeleteTools transactionDeleteTools,
                                 TransactionUpdateTools transactionUpdateTools,
                                 TransactionQueryTools transactionQueryTools,
                                 GoalCreateTools goalCreateTools,
                                 GoalDeleteTools goalDeleteTools,
                                 GoalUpdateTools goalUpdateTools,
                                 GoalQueryTools goalQueryTools,
                                 UserQueryTools userQueryTools) {
        return ChatClient.builder(chatModel)
                .defaultTools(transactionCreateTools, transactionDeleteTools
                        , transactionUpdateTools, transactionQueryTools
                        , goalCreateTools, goalDeleteTools
                        , goalUpdateTools, goalQueryTools
                        , userQueryTools)
                .build();
    }
}
