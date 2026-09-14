package com.example.spring_ai_demo.tool.songs.dto;

import java.time.LocalDate;

public record ReleaseDateInfo(
        LocalDate date,
        boolean exact,
        String originalStatement
) {
}
