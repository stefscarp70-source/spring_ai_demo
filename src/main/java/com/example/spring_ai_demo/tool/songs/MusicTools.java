package com.example.spring_ai_demo.tool.songs;

import com.example.spring_ai_demo.tool.songs.dto.UpcomingAlbumRequest;
import com.example.spring_ai_demo.tool.songs.dto.UpcomingAlbumsResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MusicTools {

    private final ChatClient researchChatClient;

    public MusicTools(ChatClient researchChatClient) {
        this.researchChatClient = researchChatClient;
    }

    @Tool(description= """
            Searches for upcoming album releases for a list of music artists.
            
            An upcoming album is an album that has been officially announced
            and has a planned release date within the requested number of months.
    
            Ignore rumours, speculation, cancelled albums and albums that have
            already been released.
    
            Return the artist, album title, expected release date when known,
                    and an assessment of how certain the information is and the statement about the release date inferred.
    
            If no matching upcoming albums are found, return an empty albums list.
            """)
    public UpcomingAlbumsResponse findUpcomingAlbums(
            UpcomingAlbumRequest request
    ) {
        return null;
    }

}
