package com.example.spring_ai_demo.tool.songs.dto;

public record TavilyResult(
        String title,
        String url,
        String content,
        Double score
) {
}
