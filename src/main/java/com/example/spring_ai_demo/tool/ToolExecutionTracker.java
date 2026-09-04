package com.example.spring_ai_demo.tool;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
public class ToolExecutionTracker {

    private final List<String> tools = new ArrayList<>();

    public void record(String toolName) {
        tools.add(toolName);
    }

    public List<String> getTools() {
        return List.copyOf(tools);
    }

}
