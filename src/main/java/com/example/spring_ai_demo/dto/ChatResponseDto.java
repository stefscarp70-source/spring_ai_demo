package com.example.spring_ai_demo.dto;

import java.util.List;

public record ChatResponseDto(
        String response,
        String model,
        List<String>tools,
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens) {
}
