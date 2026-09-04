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
import java.util.List;

@Slf4j
@Component
public class StarWarsTools {

    private final StarWarsCharacterRepository repository;
    private final VectorStore vectorStore;
    private final ToolExecutionTracker tracker;
    private final StarWarsReranker reranker;

    public StarWarsTools(StarWarsCharacterRepository repository, VectorStore vectorStore, ToolExecutionTracker tracker, StarWarsReranker reranker) {
        this.repository = repository;
        this.vectorStore = vectorStore;
        this.tracker = tracker;
        this.reranker = reranker;
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

    public static final double DOC_THRESHOLD = 0.20;

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
        List<Document> filteredDocuments = documents.stream()
                .filter(document -> document.getScore() != null && document.getScore() >= DOC_THRESHOLD)
                .toList();
        filteredDocuments.forEach(document ->
                log.debug("  >> ACCEPTED score: {} | {}", document.getScore(), document.getMetadata().get("name"))
        );
        log.debug("  --------------------");
        documents.stream()
                .filter(doc -> doc.getScore()<DOC_THRESHOLD)
                .peek(document -> log.debug("  >> score: {} | {}", document.getScore(), document.getMetadata().get("name"))
        );

        log.debug("  ....................");
        List<Document> rerankedDocs = reranker.rerank(query, filteredDocuments);
        rerankedDocs.forEach(document ->
                log.debug("  >> Reranked score: {} | {}", document.getScore(), document.getMetadata().get("name"))
        );

        return rerankedDocs.stream()
                .map(Document::getText)
                .toList();
    }
}
