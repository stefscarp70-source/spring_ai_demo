package com.example.spring_ai_demo.tool.songs.dto;

import java.util.List;

public record RecentAlbumResponse(
        UpcomingAlbum album,
        AlbumDetails album_details,
        StatusSearch status
) {

}
