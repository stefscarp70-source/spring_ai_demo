package com.example.spring_ai_demo.tool.songs.dto;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;

import java.util.Collections;

public record RunResult(
        String status,
        AgentAnswer answer,
        int steps,
        long promptTokens,
        long completionTokens,
        long totalTokens
) {
    public static RunResult budgetExceeded(int maxSteps, long total) {
        String error = "Exceeded max steps";
        AgentAnswer answer = new AgentAnswer(error, null, null, Collections.emptyList());
        return new RunResult(error, answer, maxSteps, 0, 0, total);
    }

    public static RunResult error(String error, int steps) {
        AgentAnswer agentAnswer = new AgentAnswer(error,
                null,
                null,
                Collections.emptyList());
        return new RunResult(error,  agentAnswer, steps, 0, 0, 0);
    }

    public static RunResult response(ChatResponse response, int steps, BeanOutputConverter<AgentAnswer>converter, long total) {
        String rawResponse = response.getResult().getOutput().getText();
        AgentAnswer agentAnswer = converter.convert(rawResponse);
        return new RunResult("SUCCESS",
            agentAnswer,
                steps, 0, 0, total);
    }

    public static RunResult responseFromTool(RecentAlbumResponse response, int steps, long total) {
        AgentAnswer agentAnswer = new AgentAnswer("Track list found",
                response.album_details().album_title(),
                response.album().release_date(),
                response.album_details().track_list());
        return new RunResult("SUCCESS",
                agentAnswer,
                steps, 0, 0, total);
    }
}
