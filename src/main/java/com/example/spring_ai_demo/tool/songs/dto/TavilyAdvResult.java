package com.example.spring_ai_demo.tool.songs.dto;

public record TavilyAdvResult (
        String title,
        String url,
        String content,
        String raw_content,
        Double score
) {
}
