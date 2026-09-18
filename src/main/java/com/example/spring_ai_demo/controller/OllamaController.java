package com.example.spring_ai_demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ollama")
public class OllamaController {

    private final ChatClient chatClient;

    public OllamaController(OllamaChatModel ollamaChatModel) {
        this.chatClient = ChatClient.builder(ollamaChatModel).build();
    }

    @GetMapping("test")
    public String test(@RequestParam String question) {
        return chatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }
}
