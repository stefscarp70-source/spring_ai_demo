package com.example.spring_ai_demo.controller;

import com.example.spring_ai_demo.StarWarsCharacterRepository;
import com.example.spring_ai_demo.dto.ChatResponseDto;
import com.example.spring_ai_demo.model.StarWarsCharacter;
import com.example.spring_ai_demo.tool.cooking.OllamaModelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ChatClient ollamaClient;
    private final StarWarsCharacterRepository repository;

    public ChatController(ChatClient chatClient, ChatClient ollamaChatClient, StarWarsCharacterRepository repository) {
        this.chatClient = chatClient;
        this.ollamaClient = ollamaChatClient;
        this.repository = repository;
    }

    @GetMapping("/api/characters")
    public List<StarWarsCharacter> characters() {
        return repository.findAll();
    }

    @GetMapping("/api/chat")
    public ChatResponseDto chat(@RequestParam String question) {
        ChatResponse response =  chatClient
                .prompt()
                .user(question)
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

    @GetMapping("/api/simple")
    public ChatResponseDto chatOllama(@RequestParam String question, @RequestParam String model) {
        log.info("Querying Ollama, with model: {}...", model);

        OllamaModelEnum modelEnum = OllamaModelEnum.valueOf(model);
        ChatResponse response;

        if (modelEnum==OllamaModelEnum.GPT) {
            //return ChatResponseDto.error("GPT not supported for simple queries", modelEnum.getModelName());
            response = chatClient
                    .prompt()
                    .user(question)
                    .call()
                    .chatResponse();
        } else {
            OllamaChatOptions chatOptionsOll = OllamaChatOptions.builder()
                    .model(modelEnum.getModelName())
                    .temperature(0.0)
                    .build();
            Prompt prompt = new Prompt(
                    List.of(new UserMessage("")),
                    chatOptionsOll //Ollama
            );

            response =  ollamaClient
                    .prompt(prompt)
                    .user(question)
                    .call()
                    .chatResponse();
        }

        Usage usage = response.getMetadata().getUsage();
        log.info("  >> token: {}", usage.getTotalTokens());

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
