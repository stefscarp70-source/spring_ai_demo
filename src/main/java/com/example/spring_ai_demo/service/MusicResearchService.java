package com.example.spring_ai_demo.service;

import com.example.spring_ai_demo.tool.songs.dto.AlbumDetails;
import com.example.spring_ai_demo.tool.songs.dto.TavilyAdvResult;
import com.example.spring_ai_demo.tool.songs.dto.TavilySearchAdvResponse;
import com.example.spring_ai_demo.tool.songs.dto.UpcomingAlbum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MusicResearchService {

    private final ChatClient researchChatClient;
    private final WebsearchService websearchService;

    private final StructuredOutputConverter<UpcomingAlbum> converter;
    private final StructuredOutputConverter<AlbumDetails> detConverter;

    public MusicResearchService(ChatClient researchChatClient, WebsearchService websearchService) {
        this.researchChatClient = researchChatClient;
        this.websearchService = websearchService;
        converter = new BeanOutputConverter<>(UpcomingAlbum.class);
        detConverter = new BeanOutputConverter<>(AlbumDetails.class);
    }

    public UpcomingAlbum webSearch(String artist, int monthBefore, int monthsAhead) {
        String tavAnswer = websearchService.searchAlbum(artist).answer();
        //String tavAnswer = websearchService.testSearch(artist).answer();
        //String tavAnswer = websearchService.testSearch(artist).answer();
        log.info("    tavily result: {}", tavAnswer);

        ChatResponse response = researchChatClient
                .prompt()
                .user("""
                        We need to determine whether the following artist has either a recent released album in the  past %s months
                        or an upcoming album in the next %s months.
                        
                        Artist:
                        %s
                    
                        Web research performed by Tavily:
                        %s
                    
                        Based ONLY on the web research above, determine whether an upcoming
                        album has been announced.
                    
                        Return the result using the requested structured output.
                        
                        If found nothing just answer with 'No upcoming album'.
                        If found use yes field as true and try to fill all the output. If multiple album consider just the first.
                                                
                        For each matching album provide:
                        - artist
                        - album title
                        - release date in yyyy-MM-dd, if known
                        - assessment
                        
                        Assessment must be one of:
                        CONFIRMED, ANNOUNCED, EXPECTED, RUMOURED, UNCERTAIN.
                        
                        Return only the requested information.
                        """.formatted(monthBefore, monthsAhead, artist, tavAnswer))
                .call()
                .chatResponse();

        String rawResponse = response.getResult().getOutput().getText();
        log.info("    RAW answer: {}", rawResponse);
        Usage usage = response.getMetadata().getUsage();
        log.info("    Prompt tokens: {}", usage.getPromptTokens());
        log.info("    Completion tokens: {}", usage.getCompletionTokens());
        log.info("    Total tokens: {}", usage.getTotalTokens());

        if (rawResponse.startsWith("No upcoming") || rawResponse.contains("\"yes\":false")) {
            return UpcomingAlbum.empty(artist);
        } else {
            UpcomingAlbum album = converter.convert(response
                    .getResult()
                    .getOutput()
                    .getText());

            return album;
        }
    }
    public AlbumDetails detailsSearch(String artist, String albumTitle) {
        TavilySearchAdvResponse tavResponse = websearchService.getAlbumDetails(artist, albumTitle);
        String researchContext = """
            Tavily search answer:
            %s
        
            Raw web content from the search results:
            %s
            """.formatted(
                    tavResponse.answer(),
                    tavResponse.results().stream()
                            .limit(2)
                            .map(TavilyAdvResult::raw_content)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("\n\n--- SOURCE ---\n\n"))
        );

        log.info("    tav answer: {}", tavResponse.answer());

        ChatResponse response = researchChatClient
                .prompt()
                .user("""
                      The web research below in web_result may contain a large amount of
                      irrelevant information.
                      Your task is to identify the complete track list of the album %s.

                      Extract ONLY information that is actually present in the web research.
                      Do not invent or infer missing tracks.

                      The tracklist may appear inside raw web content and may be surrounded by unrelated text.
                        Use field "track_list" to put the track list, but MUST be an array of strings.
                        Each track title must be a separate element of the array.                        
                        Do NOT return Markdown. Do NOT number the tracks.
                        Do NOT return headings or explanatory text.
                      Put in output also 
                      - artist
                      - album_title  
                      web_result: %s
                      """.formatted(albumTitle, researchContext))
                .call()
                .chatResponse();

        String rawResponse = response.getResult().getOutput().getText();
        log.info("    RAW answer: {}", rawResponse);
        Usage usage = response.getMetadata().getUsage();
        log.info("    Prompt tokens: {}", usage.getPromptTokens());
        log.info("    Completion tokens: {}", usage.getCompletionTokens());
        log.info("    Total tokens: {}", usage.getTotalTokens());

        try {
            return detConverter.convert(rawResponse);
        } catch (Exception e) {
            log.warn("Unable to parse album details response: {}", rawResponse, e);
            return AlbumDetails.empty(artist, albumTitle);
        }
    }

    public UpcomingAlbum researchUpcomingAlbum(
            String artist,
            int monthsBefore, int monthsAhead
    ) {
        log.info("  Searching for {}...", artist);
        ChatResponse response = researchChatClient
                .prompt()
                .user("""
                        Find upcoming album release for this artist:
                        
                        %s
                        
                        Consider albums either just released in the previous %d months
                        or to be expected to be released within the next %d months.
                        Perform a web search before deciding that no upcoming album exists, using these steps
                        1. Search the web. Also the website billboard.com.
                        2. Analyze the search results.
                        3. Determine whether an upcoming album exists.
                        4. Only then return found=false if appropriate.
                        
                        If found nothing just answer with 'No upcoming album'.
                        If found use Yes field as true and try to fill all the output. 
                                                
                        For each matching album provide:
                        - album title
                        - expected release date, if known
                        - assessment
                        
                        Assessment must be one of:
                        CONFIRMED, ANNOUNCED, EXPECTED, RUMOURED, UNCERTAIN.
                        
                        Return only the requested information.
                        """.formatted(artist, monthsBefore, monthsAhead))
                .call()
                .chatResponse();

        String rawResponse = response.getResult().getOutput().getText();
        log.info("    RAW answer: {}", rawResponse);
        Usage usage = response.getMetadata().getUsage();
        log.info("    Prompt tokens: {}", usage.getPromptTokens());
        log.info("    Completion tokens: {}", usage.getCompletionTokens());
        log.info("    Total tokens: {}", usage.getTotalTokens());

        if (rawResponse.startsWith("No upcoming")) {
            return UpcomingAlbum.empty(artist);
        } else {
            UpcomingAlbum album = converter.convert(response
                    .getResult()
                    .getOutput()
                    .getText());

            return album;
        }
    }
}
