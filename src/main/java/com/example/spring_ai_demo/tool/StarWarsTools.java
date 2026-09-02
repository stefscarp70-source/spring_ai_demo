package com.example.spring_ai_demo.tool;

import com.example.spring_ai_demo.StarWarsCharacterRepository;
import com.example.spring_ai_demo.model.StarWarsCharacter;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StarWarsTools {

    private final StarWarsCharacterRepository repository;
    private final SimpleVectorStore vectorStore;
    private final ToolExecutionTracker tracker;

    public StarWarsTools(StarWarsCharacterRepository repository, SimpleVectorStore vectorStore, ToolExecutionTracker tracker) {
        this.repository = repository;
        this.vectorStore = vectorStore;
        this.tracker = tracker;
    }

    @Tool(description= """
            Cerca nel database PostgreSQL tutti i personaggi Star Wars
              il cui campo Homeworld è esattamente uguale al pianeta indicato.
              Restituisce l'elenco completo dei personaggi trovati.
              Dopo aver ricevuto il risultato, utilizzalo per formulare
              la risposta all'utente.
            """)
    public List<StarWarsCharacter>  findCharByHomeworld(String homeworld) {
        tracker.record("findCharByHomeworld");
        System.out.println(">>> TOOL CHIAMATO: findCharactersByHomeworld(" + homeworld + ")");
        List<StarWarsCharacter>result =  repository.findByHomeworld(homeworld);
        System.out.println(">>> RISULTATI: " + result.size());

        return result;
    }

    @Tool(description = """
        Esegue una ricerca semantica sui personaggi di Star Wars.
        Utilizza questo strumento quando la domanda richiede
        informazioni semanticamente correlate ai personaggi
        e non una condizione strutturata esatta.
        """)
    public List<String> searchCharacters(String query) {
        tracker.record("searchCharacters LLM");

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .build()
        );

        return documents.stream()
                .map(Document::getText)
                .toList();
    }
}
