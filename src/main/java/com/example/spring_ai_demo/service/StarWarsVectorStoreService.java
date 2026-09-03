package com.example.spring_ai_demo.service;

import com.example.spring_ai_demo.StarWarsCharacterRepository;
import com.example.spring_ai_demo.model.StarWarsCharacter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StarWarsVectorStoreService {

    private final StarWarsCharacterRepository repository;
    private final VectorStore store;

    public StarWarsVectorStoreService(StarWarsCharacterRepository repository, VectorStore store) {
        this.repository = repository;
        this.store = store;
    }

    public long indexCharacters() {

        List<Document> documents = repository.findAll()
                .stream()
                .map(this::toDocument)
                .toList();

        store.add(documents);
        return documents.size();
    }

    private Document toDocument(StarWarsCharacter character) {

        String content = """
                Name: %s
                Height: %s
                Mass: %s
                Hair color: %s
                Skin color: %s
                Eye color: %s
                Birth year: %s
                Sex: %s
                Gender: %s
                Homeworld: %s
                Species: %s
                Films: %s
                Vehicles: %s
                Starships: %s
                """.formatted(
                character.name(),
                character.height(),
                character.mass(),
                character.hairColor(),
                character.skinColor(),
                character.eyeColor(),
                character.birthYear(),
                character.sex(),
                character.gender(),
                character.homeworld(),
                character.species(),
                character.films(),
                character.vehicles(),
                character.starships()
        );

        String documentId = UUID.nameUUIDFromBytes(
                ("star-wars-" + character.id()).getBytes(StandardCharsets.UTF_8)
        ).toString();

        return new Document(
                documentId,
                content,
                prepareMetadata(character)
        );
    }

    //Metodo per aggiungere alcuni metadata se disponibili
    private Map prepareMetadata(StarWarsCharacter character) {
        Map<String, Object> metadata = new HashMap<>();

        if (character.homeworld() != null) {
            metadata.put("homeworld", character.homeworld());
        }
        if (character.species() != null) {
            metadata.put("species", character.species());
        }

        return metadata;
    }

    //@Override
    public void run(String... args) {
        indexCharacters();

        System.out.println(
                "Indicizzati " + repository.count() + " personaggi Star Wars"

        );
    }
}
