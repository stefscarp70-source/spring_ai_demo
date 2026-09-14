package com.example.spring_ai_demo.tool.songs.dto;

import java.time.LocalDate;

public record UpcomingAlbum(
        String artist,
        boolean yes,
        String album_title,
        //ReleaseDateInfo releaseDate,
        LocalDate release_date,
        MusicSource source,
        ReleaseAssessment assessment
) {
    public static UpcomingAlbum empty(String artist) {
        return new UpcomingAlbum(artist, false, null, null, null, ReleaseAssessment.CONFIRMED);
    }
}
