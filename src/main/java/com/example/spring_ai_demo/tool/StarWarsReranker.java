package com.example.spring_ai_demo.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class StarWarsReranker {

    private final ChatClient chatClient;

    public StarWarsReranker(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<Document> rerank(String query, List<Document>documents) {
        if (documents.isEmpty()) {
            return  documents;
        }

        String candidates = IntStream.range(0, documents.size())
                .mapToObj(i -> """
                        DOCUMENT %d:
                        %s
                        """.formatted(
                        i + 1,
                        documents.get(i).getText()))
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                Devi effettuare il reranking dei documenti rispetto alla domanda.

                DOMANDA:
                %s

                DOCUMENTI:
                %s

                Per ogni documento valuta quanto è rilevante per rispondere
                    alla domanda.
            
                    Usa una scala da 0.0 a 1.0:
                    - 1.0 = altamente rilevante
                    - 0.5 = parzialmente rilevante
                    - 0.0 = non rilevante
            
                    Ordina i documenti dal più rilevante al meno rilevante.
            
                    Restituisci tutti i documenti una sola volta,
                    mantenendo l'ID originale.
            
                    La relevance deve rappresentare la rilevanza del documento
                    rispetto alla domanda, non la qualità generale del documento.
                """.formatted(query, candidates);

        RerankResult result = chatClient
                .prompt()
                .user(prompt)
                .call()
                .entity(RerankResult.class);

        log.debug("Reranking - query={}", query);
        log.debug("Reranking - ordine={}", result.documentIds());

        return result.documentIds().stream()
                .peek(r -> log.debug("    reranked relev: {} | {}", r.relevance(), documents.get(r.id()-1).getMetadata().get("name")))
                .map(r -> documents.get(r.id()-1))
                .toList();
    }

}
