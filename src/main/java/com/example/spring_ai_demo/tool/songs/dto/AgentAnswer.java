package com.example.spring_ai_demo.tool.songs.dto;

import java.util.List;

public record AgentAnswer(
        String answer,
        String albumTitle,
        List<String> trackList
) {
}
