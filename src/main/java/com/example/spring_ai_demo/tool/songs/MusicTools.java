package com.example.spring_ai_demo.tool.songs;

import com.example.spring_ai_demo.service.MusicResearchService;
import com.example.spring_ai_demo.tool.songs.dto.AlbumDetails;
import com.example.spring_ai_demo.tool.songs.dto.RecentAlbumResponse;
import com.example.spring_ai_demo.tool.songs.dto.StatusSearch;
import com.example.spring_ai_demo.tool.songs.dto.UpcomingAlbum;
import com.example.spring_ai_demo.tool.songs.dto.UpcomingAlbumRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MusicTools {


    private final MusicResearchService musicResearchService;

    public MusicTools(MusicResearchService musicResearchService) {
        this.musicResearchService = musicResearchService;
    }

    @Tool( name = "findRecentAlbum",
            description= """
            Searches for recent album of an artist, 
            that is to say either upcoming or just released album of an artist.
            
            An upcoming album is an album that has been officially announced
            and has a planned release date within the requested number of months.
            A just released album is an album released in the last number of months.
    
            Ignore rumours, speculation, cancelled albums and albums that have
            already been released.
    
            Return the artist, album title, expected release date when known,
                    and an assessment of how certain the information is and the statement about the release date inferred.
            Use default monthsAhead and monthBefore equal to 12.                    
    
            status field can be SUCCESS, meaning album found, EMPTY, meaning no album found, or ERROR.
            """)
    public RecentAlbumResponse findRecentAlbum(
            UpcomingAlbumRequest request
    ) {
        log.info("      Tool findRecentAlbum: request: {}", request);
        if (request==null) return RecentAlbumResponse.warn("Invalid null request as input argument");

        UpcomingAlbum album = musicResearchService.webSearch(request.artist(), request.monthsBefore(), request.monthsAhead());
        if (!album.yes()) {
            return new RecentAlbumResponse(album, null, StatusSearch.EMPTY, "No album found");
        } else if (album==null) {
            return RecentAlbumResponse.error("No album found");
        } else {
            return new RecentAlbumResponse(album, null, StatusSearch.SUCCESS, null);

        }
    }

    @Tool(
            name = "getAlbumDetails",
            description = """
            Finds the complete track list of a specific album of an artist.
            
            status field can be SUCCESS, meaning album found with track list, EMPTY, meaning no album found, or ERROR.
            """
    )
    public RecentAlbumResponse getAlbumDetails(UpcomingAlbum album) {
        if (album==null) return RecentAlbumResponse.warn("Invalid null album as input argument");
        if (album.album_title()==null) return RecentAlbumResponse.error("album title is missing, the search MUST terminate with no album found");

        AlbumDetails details = musicResearchService.detailsSearch(album.artist(), album.album_title());
        if (details.track_list().isEmpty()) {
            return new RecentAlbumResponse(album, details, StatusSearch.EMPTY, "track list empty for album "+album.album_title());
        } else if (details==null) {
            return RecentAlbumResponse.error("No track list found for album "+album.album_title());
        } else {
            return new RecentAlbumResponse(album, details, StatusSearch.SUCCESS, null);

        }
    }

}
