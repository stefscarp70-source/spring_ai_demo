package com.example.spring_ai_demo.controller;

import com.example.spring_ai_demo.dto.OpenAiUsageResponse;
import com.example.spring_ai_demo.dto.OpenAiUsageResult;
import com.example.spring_ai_demo.dto.TotalUsageDto;
import com.example.spring_ai_demo.service.StarWarsVectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@RestController
public class EmbeddingController {

    private final EmbeddingModel model;
    private final VectorStore store;
    private final StarWarsVectorStoreService service;
    private final RestClient adminClient;

    public EmbeddingController(@Qualifier("openAiEmbeddingModel") EmbeddingModel model, VectorStore store, StarWarsVectorStoreService service, RestClient.Builder restClientBuilder) {
        this.model = model;
        this.store = store;
        this.service = service;
        this.adminClient = restClientBuilder
                .baseUrl("https://api.openai.com")
                .build();
    }

    @GetMapping("/api/vector/init")
    public Map<String, Object> index() {

        long count = service.indexCharacters();

        return Map.of(
                "indexed", count
        );
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

    @Value("${spring.ai.openai.admin-api-key}")
    private String adminApiKey;

    @GetMapping("/admin/usage")
    public TotalUsageDto usage() {
        long ndays = 20;
        long endTime = Instant.now().getEpochSecond();
        long startTime = Instant.now()
                .minus(ndays, ChronoUnit.DAYS)
                .getEpochSecond();
        OpenAiUsageResponse response = adminClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/organization/usage/completions")
                        .queryParam("start_time", startTime)
                        .queryParam("end_time", endTime)
                        .queryParam("bucket_width", "1d")
                        .queryParam("limit", 30)
                        .build())
                .header("Authorization", "Bearer " + adminApiKey)
                .retrieve()
                .body(OpenAiUsageResponse.class);

        List<UsageBucketDto> buckets = response.data().stream()
                .peek(bucket -> log.info("day: {} >>> total: {}", bucket.end_time_iso(), estimateTotal(bucket.results())))
                .filter(bucket -> !bucket.results().isEmpty())
                .map(bucket -> {
                    OpenAiUsageResult result = bucket.results().get(0);

                    return new UsageBucketDto(
                            bucket.end_time_iso(),
                            result.input_tokens(),
                            result.output_tokens()
                    );
                })
                .toList();

        long totalInputTokens = buckets.stream()
                .mapToLong(UsageBucketDto::inputTokens)
                .sum();

        long totalOutputTokens = buckets.stream()
                .mapToLong(UsageBucketDto::outputTokens)
                .sum();

        return new TotalUsageDto(
                totalInputTokens, totalOutputTokens,
                totalInputTokens + totalOutputTokens
        );
    }

    private long estimateTotal(List<OpenAiUsageResult> results) {
        if (results.isEmpty()) {
            return 0L;
        } else {
            OpenAiUsageResult result = results.get(0);

            return result.input_tokens() + result.output_tokens();
        }
    }
}
