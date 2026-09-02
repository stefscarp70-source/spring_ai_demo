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
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class RagController {

    private final SimpleVectorStore vectorStore;
    private final ChatClient chat;
    private final StarWarsTools tools;
    private final OpenAiChatModel openAiChatModel;
    private final ToolExecutionTracker tracker;

    public RagController(SimpleVectorStore vectorStore, ChatClient chat, StarWarsTools tools, OpenAiChatModel openAiChatModel, ToolExecutionTracker tracker) {
        this.vectorStore = vectorStore;
        this.chat = chat;
        this.tools = tools;
        this.openAiChatModel = openAiChatModel;
        this.tracker = tracker;
    }

    @GetMapping("/api/rag")
    public ChatResponseDto rag(@RequestParam String question, @RequestParam Integer docs) {
        int numPK = 5;
        if (docs!=null && docs>0) {
            numPK = docs;
        }

        OpenAiChatOptions options = openAiChatModel.getOptions();

        System.out.println(">>> MODEL: " + options.getModel());
        System.out.println(">>> REASONING EFFORT: " + options.getReasoningEffort());
        System.out.println(">>> TOOL CHOICE: " + options.getToolChoice());

        ToolCallback[] stools = ToolCallbacks.from(tools);

        for (ToolCallback tool : stools) {
            System.out.println(
                    ">>> TOOL REGISTRATO: "
                            + tool.getToolDefinition().name());
        }

        /*
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(numPK)
                        .build()
        );

        if (docs!=null && docs>0) {
            documents.forEach(
                    d -> System.out.println( compactDocument(d))
            );
            System.out.println( "-------------------");
        }


        String context = documents.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a + "\n\n" + b);

         */


        ChatResponse response = chat
                .prompt()
//                .user("""
//                        Rispondi alla domanda utilizzando le informazioni presenti nel CONTEXT
//                        ma anche considerando se usare il tool
//
//                        Se la risposta non è presente nel CONTEXT,
//                        dichiara esplicitamente di non avere
//                        informazioni sufficienti.
//
//                        CONTEXT:
//                        %s
//
//                        DOMANDA:
//                        %s
//                        """.formatted(context, question))
                .user(question)
                .tools(tools)
                .options(OpenAiChatOptions.builder()
                        //.toolChoice("required")
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
