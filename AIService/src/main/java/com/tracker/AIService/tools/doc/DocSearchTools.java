package com.tracker.AIService.tools.doc;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocSearchTools {

    private final VectorStore vectorStore;

    @Value("${app.doc.search.top-k:4}")
    private int topK;

    @Value("${app.doc.search.similarity-threshold:0.2}")
    private double threshold;

    @Tool(description = """
        Search the finance tracker documentation.
        Use for questions about how the app works, its features and how to use them.
        Returns relevant documentation fragments.
        """)
    public List<String> searchDocs(String query) {
        return vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(query)
                                .topK(topK)
                                .similarityThreshold(threshold)
                                .build())
                .stream()
                .map(Document::getText)
                .toList();
    }
}