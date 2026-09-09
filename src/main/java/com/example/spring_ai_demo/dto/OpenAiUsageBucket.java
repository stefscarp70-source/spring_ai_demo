package com.example.spring_ai_demo.dto;

import java.util.List;

public record OpenAiUsageBucket(
        String end_time_iso,
        List<OpenAiUsageResult> results
) {
}
