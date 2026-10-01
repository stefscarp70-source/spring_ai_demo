package com.example.spring_ai_demo.dto;

import java.util.Collections;
import java.util.List;

public record ChatResponseDto(
        String response,
        String model,
        List<String>tools,
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens) {

    public static ChatResponseDto error(String message, String model) {
        return new ChatResponseDto(message, model, Collections.emptyList(), 0, 0, 0);
    }
}
