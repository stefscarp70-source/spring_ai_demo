package com.example.spring_ai_demo.dto;

public record TotalUsageDto(
        long inputTokens,
        long outputTokens,
        long totalTokens
) {
}
