package com.example.spring_ai_demo.controller;

public record UsageBucketDto(
        String time_iso,
        long inputTokens,
        long outputTokens
) {
}
