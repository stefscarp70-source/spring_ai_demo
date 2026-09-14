package com.example.spring_ai_demo.service;

import com.example.spring_ai_demo.tool.songs.dto.AlbumDetails;
import com.example.spring_ai_demo.tool.songs.dto.TavilyResult;
import com.example.spring_ai_demo.tool.songs.dto.TavilySearchAdvResponse;
import com.example.spring_ai_demo.tool.songs.dto.TavilySearchRequest;
import com.example.spring_ai_demo.tool.songs.dto.TavilySearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Service
public class WebsearchService {

    private final RestClient restClient;


    public WebsearchService(RestClient.Builder restClientBuilder, @Value("${tavily.api-key}") String apiKey) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.tavily.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TavilySearchResponse searchAlbum(String artist) {
        String query = String.format("%s upcoming album", artist);

        return basicSearch(query, false, TavilySearchResponse.class);
    }

    public TavilySearchAdvResponse getAlbumDetails(String artist, String albumTitle) {
        String query = String.format("%s %s album complete song list", artist, albumTitle);

        return basicSearch(query, true, TavilySearchAdvResponse.class);
    }

    private <T> T basicSearch(String query, boolean raw, Class<T>clazz) {
        TavilySearchRequest req = new TavilySearchRequest(
                query, "basic", 5, true, raw
        );

        return restClient.post()
                .uri("/search")
                .body(req)
                .retrieve()
                .body(clazz);
    }

    private final String mockedAnswer = "Nickelback's upcoming album, \"Everything Under the Sun,\" is set for release on October 30, 2026. The album features the single \"Rattle the Cage.\" Pre-orders are available.";
    private final String ans2 = "Mariah Carey's sixteenth studio album, \"Here For It All,\" was released on September 26, 2025. The album features collaborations with artists like Anderson .Paak and The Clark Sisters.";
    public TavilySearchResponse testSearch(String query) {
        TavilyResult res = new TavilyResult(
                "title", "url", mockedAnswer, 0.99
        );

        return new TavilySearchResponse(
                query, ans2, Arrays.asList(res)
        );
    }
}
