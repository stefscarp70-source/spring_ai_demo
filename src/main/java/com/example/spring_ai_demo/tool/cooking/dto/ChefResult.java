package com.example.spring_ai_demo.tool.cooking.dto;

import com.example.spring_ai_demo.tool.dto.ToolInvocation;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.Collections;
import java.util.List;

public record ChefResult(
        String status,
        List<IngredientDto> list,
        String response,
        List<ToolInvocation> tools,
        int steps,
        Double totalCost,
        long totalTokens
) {
    public static ChefResult budgetExceeded(int maxSteps, long total) {
        String error = "Exceeded max steps";
        List<IngredientDto> list = Collections.emptyList();
        List<ToolInvocation> tools = Collections.emptyList();
        return new ChefResult(error, list, error, tools, maxSteps, null, total);
    }

    public static ChefResult error(String error, int steps) {
        List<IngredientDto> list = Collections.emptyList();
        List<ToolInvocation> tools = Collections.emptyList();
        return new ChefResult(error,  list, null, tools, steps, null, 0);
    }

    public static ChefResult response(ChatResponse response, int steps, List<IngredientDto>list, Double cost, long total) {
        String rawResponse = response.getResult().getOutput().getText();
        List<ToolInvocation> tools = Collections.emptyList();
        return new ChefResult("SUCCESS",
            list, rawResponse, tools,
                steps, cost, total);
    }

    public static ChefResult simpleResponse(ChatResponse response, int steps, long total, List<ToolInvocation> tools) {
        String rawResponse = response.getResult().getOutput().getText();
        return new ChefResult("SUCCESS",
                Collections.emptyList(), rawResponse,
                tools, steps, null, total);
    }

}
