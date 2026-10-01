package com.example.spring_ai_demo.tool.cooking;

public enum OllamaModelEnum {
    LLAMA3("llama3.1:8b-instruct-q4_K_M"),
    QWEN3("qwen3:8b"),
    QWEN3_4B("qwen3:4b"),
    GEMMA4("gemma4:e4b"),
    GPT("gpt");;

    private final String modelName;

    public String getModelName() {
        return modelName;
    }

    public static OllamaModelEnum fromName(String name) {
        for(OllamaModelEnum v: OllamaModelEnum.values()) {
            if (name.equals(v.getModelName()))
                return v;
        }
        return null;
    }

    OllamaModelEnum(String name) {
        this.modelName = name;
    }
};
