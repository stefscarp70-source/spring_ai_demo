package com.example.spring_ai_demo.controller;

import com.example.spring_ai_demo.StarWarsCharacterRepository;
import com.example.spring_ai_demo.dto.ChatResponseDto;
import com.example.spring_ai_demo.model.StarWarsCharacter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final StarWarsCharacterRepository repository;

    public ChatController(ChatClient.Builder builder, StarWarsCharacterRepository repository) {
        this.chatClient = builder.build();
        this.repository = repository;
    }

    @GetMapping("/api/characters")
    public List<StarWarsCharacter> characters() {
        return repository.findAll();
    }

    @GetMapping("/api/chat")
    public ChatResponseDto chat(@RequestParam String message) {
        ChatResponse response =  chatClient
                .prompt()
                .user(message)
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();

        return new ChatResponseDto(
                response.getResult().getOutput().getText(),
                response.getMetadata().getModel(),
                Collections.emptyList(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
    }

    @GetMapping("/api/chat/character")
    public ChatResponseDto character(
            @RequestParam String name,
            @RequestParam String question) {

        StarWarsCharacter character = repository
                .findByName(name)
                .orElseThrow();

        String context = """
            Nome: %s
            Altezza: %s
            Massa: %s
            Colore capelli: %s
            Colore pelle: %s
            Colore occhi: %s
            Pianeta natale: %s
            """.formatted(
                character.name(),
                character.height(),
                character.mass(),
                character.hairColor(),
                character.skinColor(),
                character.eyeColor(),
                character.homeworld()
        );

        ChatResponse response = chatClient
                .prompt()
                .user("""
                    Rispondi alla domanda utilizzando esclusivamente
                    le informazioni presenti nel CONTEXT.

                    Se la risposta non è presente nel CONTEXT,
                    rispondi che non disponi dell'informazione.

                    CONTEXT:
                    %s

                    DOMANDA:
                    %s
                    """.formatted(context, question))
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();

        return new ChatResponseDto(
                response.getResult().getOutput().getText(),
                response.getMetadata().getModel(),
                Collections.emptyList(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
    }

}
