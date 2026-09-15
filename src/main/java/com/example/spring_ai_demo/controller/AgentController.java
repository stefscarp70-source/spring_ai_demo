package com.example.spring_ai_demo.controller;

import com.example.spring_ai_demo.service.AgentService;
import com.example.spring_ai_demo.service.MusicResearchService;
import com.example.spring_ai_demo.service.WebsearchService;
import com.example.spring_ai_demo.tool.songs.MusicTools;
import com.example.spring_ai_demo.tool.songs.dto.AlbumDetails;
import com.example.spring_ai_demo.tool.songs.dto.RunResult;
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
    private final AgentService agentService;

    public AgentController(MusicResearchService musicResearchService, WebsearchService websearchService, MusicTools musicTools, AgentService agentService) {
        this.musicResearchService = musicResearchService;
        this.websearchService = websearchService;
        this.agentService = agentService;
    }

    @GetMapping("/tav/search")
    public TavilySearchResponse tavSearch(@RequestParam String artist) {
        return websearchService.searchAlbum(artist);
    }

    @GetMapping("/tav/details")
    public Object tavDetails(@RequestParam String artist, @RequestParam String title) {
        return websearchService.getAlbumDetails(artist, title);
    }

    @GetMapping("/agent/album")
    public UpcomingAlbum albumSearch(@RequestParam String artist, @RequestParam Integer months) {

        return musicResearchService.webSearch(artist, months, months);

    }

    @GetMapping("/agent/tracks")
    public AlbumDetails albumSearch(@RequestParam String artist, @RequestParam String title) {

        return musicResearchService.detailsSearch(artist, title);

    }

    //Run agentico principale
    @GetMapping("/agent/run")
    public RunResult agentRun(@RequestParam String artist) {

        return agentService.run(artist);

    }

}
