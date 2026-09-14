package com.example.spring_ai_demo.tool.songs.dto;

import java.util.List;

public record TavilySearchAdvResponse(
        String query,
        String answer,
        List<TavilyAdvResult> results
) {
}
