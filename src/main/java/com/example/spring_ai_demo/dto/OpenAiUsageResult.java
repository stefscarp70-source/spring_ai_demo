package com.example.spring_ai_demo.dto;

public record OpenAiUsageResult(
        long input_tokens,
        long output_tokens
) {
}
