package com.example.spring_ai_demo.tool.songs.dto;

public record TavilySearchRequest(
        String query,
        String search_depth,
        int max_results,
        boolean include_answer,
        boolean include_raw_content
) {
}
