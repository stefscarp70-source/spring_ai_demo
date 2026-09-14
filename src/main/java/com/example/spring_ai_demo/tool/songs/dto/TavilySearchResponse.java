package com.example.spring_ai_demo.tool.songs.dto;

import java.util.List;

public record TavilySearchResponse(
        String query,
        String answer,
        List<TavilyResult> results
) {
}
