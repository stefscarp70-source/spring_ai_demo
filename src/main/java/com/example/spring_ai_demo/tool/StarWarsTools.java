package com.example.spring_ai_demo.tool;

import com.example.spring_ai_demo.StarWarsCharacterRepository;
import com.example.spring_ai_demo.model.StarWarsCharacter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StarWarsTools {

    private final StarWarsCharacterRepository repository;
    private final VectorStore vectorStore;
    private final ToolExecutionTracker tracker;

    public StarWarsTools(StarWarsCharacterRepository repository, VectorStore vectorStore, ToolExecutionTracker tracker) {
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
        log.info("  TOOL CHIAMATO: findCharactersByHomeworld( {} )", homeworld);
        List<StarWarsCharacter>result =  repository.findByHomeworld(homeworld);
        log.info("    RISULTATI: {}", result.size());

        return result;
    }

    @Tool(description = """
            Cerca personaggi Star Wars usando una ricerca semantica.
            Può opzionalmente filtrare i risultati per pianeta natale e/o per specie.
        """)
    public List<String> searchCharacters(String query, String homeworld, String species) {
        tracker.record("searchCharacters LLM");
        log.info("   Tool searchCharacters con arg: ");
        log.info("       - {}", query);
        log.info("       - home: {}", homeworld);
        log.info("       - spec: {}", species);

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(5);

        List<String> filters = new ArrayList<>();
        if (homeworld != null && !homeworld.isBlank()) {
            filters.add("homeworld == '"+homeworld+"'");
        }
        if (species != null && !species.isBlank()) {
            filters.add("species == '" + species + "'");
        }
        if (!filters.isEmpty()) {
            builder.filterExpression(String.join(" && ", filters));
        }

        List<Document> documents = vectorStore.similaritySearch(builder.build());
        return documents.stream()
                .map(Document::getText)
                .toList();
    }
}
