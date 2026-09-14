package com.example.spring_ai_demo.tool.songs.dto;

import java.util.List;

public record UpcomingAlbumRequest(
        List<String> artists,
        int monthsAhead
) {
}
