package com.example.spring_ai_demo.tool.songs.dto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record AlbumDetails(
        String artist,
        String album_title,
        LocalDate release_date,
        List<String> track_list
) {
    public static AlbumDetails empty(String artist, String album_title) {
        return new AlbumDetails(artist, album_title, null, Collections.emptyList());
    }
}
