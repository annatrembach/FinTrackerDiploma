package com.tracker.AIService.services.impls;

import com.tracker.AIService.models.dto.doc.DocRequest;
import com.tracker.AIService.services.AiDocService;
import com.tracker.AIService.util.AuthContext;
import com.tracker.AIService.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDocServiceImpl implements AiDocService {

    private final VectorStore vectorStore;

    @Qualifier("docChatClient")
    private final ChatClient docChatClient;

    @Value("${app.doc.search.top-k:4}")
    private int topK;

    @Value("${app.doc.search.similarity-threshold:0.2}")
    private double similarityThreshold;

    private final JwtUtil jwtUtil;

    public String answer(DocRequest request, String token) {
        log.info("Processing doc question: {}", request.question());

        Long userId = jwtUtil.extractUserId(token);

        AuthContext.setUserId(userId);
        AuthContext.setToken(token);

        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.question())
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .filterExpression("type == 'documentation'")
                        .build()
        );

        log.debug("Found {} relevant chunks for question: {}", relevantDocs.size(), request.question());

        relevantDocs.forEach(d -> log.info("Found doc: {} with score: {}",
                d.getMetadata().get("source"), d.getScore()));

        if (relevantDocs.isEmpty()) {
            return "I couldn't find relevant information in the documentation for your question. " +
                    "Please try rephrasing, or contact support.";
        }

        String context = buildContext(relevantDocs);

        String response = docChatClient.prompt()
                .user(buildRagPrompt(context, request.question()))
                .call()
                .content();

        log.debug("Doc response generated for question: {}", request.question());
        return response;
    }

    private String buildContext(List<Document> docs) {
        return docs.stream()
                .map(doc -> {
                    String source = (String) doc.getMetadata()
                            .getOrDefault("source", "unknown");

                    return "--- [Source: " + source + "] ---\n"
                            + doc.getText();
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String buildRagPrompt(String context, String question) {
        return """
                Use the following documentation to answer the question.
                
                DOCUMENTATION CONTEXT:
                %s
                
                ---
                
                QUESTION: %s
                """.formatted(context, question);
    }
}