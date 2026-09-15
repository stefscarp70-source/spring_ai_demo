package com.example.spring_ai_demo.tool.songs.dto;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;

public record RunResult(
        String status,
        AgentAnswer answer,
        int steps,
        long promptTokens,
        long completionTokens,
        long totalTokens
) {
    public static RunResult budgetExceeded(int maxSteps, long total) {
        return new RunResult("Exceeded max steps", null, maxSteps, 0, 0, total);
    }

    public static RunResult error(String error, int steps) {
        return new RunResult(error,  null,steps, 0, 0, 0);
    }

    public static RunResult response(ChatResponse response, int steps, BeanOutputConverter<AgentAnswer>converter, long total) {
        String rawResponse = response.getResult().getOutput().getText();
        AgentAnswer agentAnswer = converter.convert(rawResponse);
        return new RunResult("SUCCESS",
            agentAnswer,
                steps, 0, 0, total);
    }
}
