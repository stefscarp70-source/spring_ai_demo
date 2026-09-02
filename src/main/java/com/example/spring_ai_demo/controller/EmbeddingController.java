package com.example.spring_ai_demo.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.IntStream;

@RestController
public class EmbeddingController {

    private final EmbeddingModel model;
    private final SimpleVectorStore store;

    public EmbeddingController(EmbeddingModel model, SimpleVectorStore store) {
        this.model = model;
        this.store = store;
    }

    @GetMapping("/api/embedding")
    public List<Float> embedding(@RequestParam String text) {
        float[] embedding = model.embed(text);
        return IntStream.range(0, embedding.length)
                .mapToObj(index -> embedding[index])
                .toList();
    }

    @GetMapping("/api/search")
    public List<Document> search(@RequestParam String query) {

        return store.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .build()
        );
    }
}
