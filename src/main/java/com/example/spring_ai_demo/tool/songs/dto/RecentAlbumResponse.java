package com.example.spring_ai_demo.tool.songs.dto;

import java.util.List;

public record RecentAlbumResponse(
        UpcomingAlbum album,
        AlbumDetails album_details,
        StatusSearch status,
        String errorMessage
) {
  public static RecentAlbumResponse error(String mess) {
      return new RecentAlbumResponse(null, null, StatusSearch.ERROR, mess);
  }

    public static RecentAlbumResponse warn(String mess) {
        return new RecentAlbumResponse(null, null, StatusSearch.WARN, mess);
    }
}
