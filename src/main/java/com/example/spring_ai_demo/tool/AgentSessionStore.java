package com.example.spring_ai_demo.tool;

import com.example.spring_ai_demo.tool.cooking.dto.IngredientDto;
import com.example.spring_ai_demo.tool.dto.ToolInvocation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentSessionStore {

    private final Map<String, List<ToolInvocation>> sessions = new ConcurrentHashMap<>();

    public void add(String sessionId, ToolInvocation invocation) {
        sessions.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(invocation);
    }

    public void add(String sessionId, String toolName, String args) {
        ToolInvocation tool = new ToolInvocation(toolName, Collections.emptyList(), args);
        sessions.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(tool);
    }
    public void add(String sessionId, String toolName, List<IngredientDto> ingredientDtos) {
        ToolInvocation tool = new ToolInvocation(toolName, ingredientDtos, null);
        sessions.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(tool);
    }


    public List<ToolInvocation> get(String sessionId) {
        return sessions.getOrDefault(sessionId, List.of());
    }

    public ToolInvocation getLastTool(String sessionId, String toolName) {
        List<ToolInvocation> tools = get(sessionId).stream()
                .filter(tool -> toolName.equals(tool.name()))
                .toList();
        if (tools.isEmpty()) return null;
        return tools.get(tools.size() - 1);
    }

}
