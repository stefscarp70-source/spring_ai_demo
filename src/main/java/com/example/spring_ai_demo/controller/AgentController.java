package com.example.spring_ai_demo.controller;

import com.example.spring_ai_demo.service.MusicResearchService;
import com.example.spring_ai_demo.service.WebsearchService;
import com.example.spring_ai_demo.tool.songs.dto.TavilySearchResponse;
import com.example.spring_ai_demo.tool.songs.dto.UpcomingAlbum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AgentController {

    private final MusicResearchService musicResearchService;
    private final WebsearchService websearchService;
    private final OpenAiChatModel openAiChatModel;

    public AgentController(MusicResearchService musicResearchService, WebsearchService websearchService, OpenAiChatModel openAiChatModel) {
        this.musicResearchService = musicResearchService;
        this.websearchService = websearchService;
        this.openAiChatModel = openAiChatModel;
    }

    @GetMapping("/tav/search")
    public TavilySearchResponse tavSearch(@RequestParam String artist) {
        return websearchService.searchAlbum(artist);
    }

    @GetMapping("/tav/details")
    public Object tavDetails(@RequestParam String artist, @RequestParam String title) {
        return websearchService.getAlbumDetails(artist, title);
    }

    @GetMapping("/agent/music")
    public UpcomingAlbum albumSearch(@RequestParam String artist, @RequestParam Integer months) {

        //return musicResearchService.researchUpcomingAlbum(artist, months, months);
        return musicResearchService.webSearch(artist, months, months);

    }

}
