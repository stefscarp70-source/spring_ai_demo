package com.example.spring_ai_demo.controller;

import com.example.spring_ai_demo.dto.ChatResponseDto;
import com.example.spring_ai_demo.tool.StarWarsTools;
import com.example.spring_ai_demo.tool.ToolExecutionTracker;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class RagController {

    private final VectorStore vectorStore;
    private final ChatClient chat;
    private final StarWarsTools tools;
    private final OpenAiChatModel openAiChatModel;
    private final ToolExecutionTracker tracker;

    public RagController(VectorStore vectorStore, ChatClient chat, StarWarsTools tools, OpenAiChatModel openAiChatModel, ToolExecutionTracker tracker) {
        this.vectorStore = vectorStore;
        this.chat = chat;
        this.tools = tools;
        this.openAiChatModel = openAiChatModel;
        this.tracker = tracker;
    }

    @GetMapping("/api/rag")
    public ChatResponseDto rag(@RequestParam String question) {

        OpenAiChatOptions options = openAiChatModel.getOptions();

        System.out.println(">>> MODEL: " + options.getModel());
        System.out.println(">>> REASONING EFFORT: " + options.getReasoningEffort());
        System.out.println(">>> TOOL CHOICE: " + options.getToolChoice());

        ChatResponse response = chat
                .prompt()
                .user(question)
                .tools(tools)
                .options(OpenAiChatOptions.builder()
                        .reasoningEffort("none")
                        )
                .call()
                .chatResponse();

        System.out.println( "-------------------");
        Usage usage = response.getMetadata().getUsage();

        return new ChatResponseDto(
                response.getResult().getOutput().getText(),
                response.getMetadata().getModel(),
                tracker.getTools(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
    }

    @GetMapping("/api/rag2")
    public ChatResponseDto rag2(@RequestParam String question, @RequestParam Integer docs) {

        OpenAiChatOptions options = openAiChatModel.getOptions();

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(docs)
                        .filterExpression("homeworld == 'Tatooine'")
                        .build()
        );
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
        String prompt = """
            Rispondi alla domanda utilizzando esclusivamente le informazioni
            presenti nel CONTEXT.
    
            Se il CONTEXT non contiene informazioni sufficienti per rispondere,
            dichiara che non hai informazioni sufficienti.
    
            CONTEXT:
            %s
    
            DOMANDA:
            %s
        """.formatted(context, question);

        ChatResponse response = chat
                .prompt()
                .user(prompt)
                .options(OpenAiChatOptions.builder()
                        .reasoningEffort("none")
                )
                .call()
                .chatResponse();

        System.out.println( "-------------------");
        Usage usage = response.getMetadata().getUsage();

        return new ChatResponseDto(
                response.getResult().getOutput().getText(),
                response.getMetadata().getModel(),
                tracker.getTools(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
    }


    private String compactDocument(Document document) {
        String text = document.getText();

        return document.getScore() +" --> "+ Arrays.stream(text.split("\\R"))
                .filter(line -> line.startsWith("Name:")
                        || line.startsWith("Homeworld:")
                        || line.startsWith("Species:"))
                .collect(Collectors.joining(" | "));
    }
}
