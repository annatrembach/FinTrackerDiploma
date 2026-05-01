package com.tracker.AIService.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.doc.ingestion.docs-path:classpath:docs/}")
    private String docsPath;

    @Value("${app.doc.ingestion.chunk-size:800}")
    private int chunkSize;

    @Value("${app.doc.ingestion.chunk-overlap:150}")
    private int chunkOverlap;

    @PostConstruct
    public void ingestDocuments() {
        if (!tableExists()) {
            log.warn("Table 'vector_store' does not exist yet — skipping ingestion. Run Flyway migration first.");
            return;
        }

        try {
            Integer existingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vector_store WHERE metadata->>'source' LIKE '%.md'",
                    Integer.class
            );

            if (existingCount != null && existingCount > 0) {
                log.info("VectorStore already has {} chunks, skipping ingestion", existingCount);
                return;
            }
        } catch (Exception e) {
            log.warn("Could not check existing chunks: {}", e.getMessage());
        }

        log.info("Starting document ingestion...");
        long start = System.currentTimeMillis();

        try {
            List<Document> allDocuments = loadAndSplitDocuments();
            if (allDocuments.isEmpty()) {
                log.warn("No documents found, skipping");
                return;
            }
            vectorStore.add(allDocuments);
            log.info("Ingestion done: {} chunks in {}ms", allDocuments.size(), System.currentTimeMillis() - start);
        } catch (IOException e) {
            log.error("Ingestion failed: {}", e.getMessage(), e);
        }
    }

    private boolean tableExists() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_name = 'vector_store'",
                    Integer.class
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private List<Document> loadAndSplitDocuments() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(docsPath + "**/*.md");

        log.info("Found {} markdown files to ingest", resources.length);

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(50)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();

        List<Document> allChunks = new ArrayList<>();

        for (Resource resource : resources) {
            try {
                log.debug("Processing file: {}", resource.getFilename());

                TextReader reader = new TextReader(resource);
                reader.getCustomMetadata().put("source", resource.getFilename());
                reader.getCustomMetadata().put("type", "documentation");

                List<Document> docs = reader.get();
                List<Document> chunks = splitter.apply(docs);

                chunks.forEach(chunk ->
                        chunk.getMetadata().putAll(Map.of(
                                "source", resource.getFilename(),
                                "type", "documentation"
                        ))
                );

                allChunks.addAll(chunks);
                log.debug("File {} split into {} chunks", resource.getFilename(), chunks.size());

            } catch (Exception e) {
                log.error("Error processing file {}: {}", resource.getFilename(), e.getMessage());
            }
        }

        return allChunks;
    }

    public void reIngestDocuments() {
        log.info("Re-ingesting all documents — clearing existing and reloading...");
        jdbcTemplate.update("DELETE FROM vector_store WHERE metadata->>'type' = 'documentation'");
        try {
            List<Document> docs = loadAndSplitDocuments();
            if (!docs.isEmpty()) {
                vectorStore.add(docs);
                log.info("Re-ingested {} chunks", docs.size());
            }
        } catch (IOException e) {
            log.error("Re-ingestion failed: {}", e.getMessage(), e);
        }
    }
}