package com.tracker.AIService.config;

import com.tracker.AIService.tools.goal.*;
import com.tracker.AIService.tools.transaction.*;
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
                .defaultTools(
                        transactionCreateTools, transactionDeleteTools,
                        transactionUpdateTools, transactionQueryTools,
                        goalCreateTools, goalDeleteTools,
                        goalUpdateTools, goalQueryTools,
                        userQueryTools)
                .build();
    }

    @Bean("analysisChatClient")
    public ChatClient analysisChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are an expert personal finance analyst.
                        
                        Your job is to analyze a user's financial data and return insights.
                        
                        Rules:
                        - Always respond ONLY with valid JSON, no markdown, no extra text.
                        - Be specific: mention exact amounts, categories, percentages.
                        - Anomaly = any single transaction > 2x the average in that category,
                          or any month where spending jumped > 50% vs previous month.
                        - Saving tips must be actionable and based on actual data provided.
                        - All amounts in the same currency as the input data.
                        - Dates in ISO-8601 format.
                        """)
                .build();
    }

    @Bean("docChatClient")
    public ChatClient docChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                    You are a helpful assistant for a personal finance tracker application.

                    Answer questions based ONLY on the provided documentation context.

                    Formatting rules:
                    - Always use clean Markdown formatting.
                    - Use headings for sections when appropriate.
                    - Use bullet point lists for features, options, and steps.
                    - Use numbered lists for instructions or sequences.
                    - Separate sections with empty lines for readability.
                    - Highlight important terms using bold formatting.
                    - Never return one large block of text.
                    - Keep responses visually structured and easy to scan.
                    - If explaining a process, use clear section titles.
                    - Use examples when useful.

                    Content rules:
                    - If the answer is not in the context, say:
                      "I don't have information about that in the documentation."
                    - Be concise and practical.
                    - Never invent features that are not mentioned in the context.
                    """)
                .build();
    }
}