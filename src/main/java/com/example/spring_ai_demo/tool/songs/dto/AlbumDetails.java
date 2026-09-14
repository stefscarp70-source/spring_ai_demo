package com.example.spring_ai_demo.tool.songs.dto;

import java.time.LocalDate;
import java.util.List;

public record AlbumDetails(
        String artist,
        String albumTitle,
        LocalDate releaseDate,
        List<String> tracks
) {
}
