package com.example.spring_ai_demo.service;

import com.example.spring_ai_demo.StarWarsCharacterRepository;
import com.example.spring_ai_demo.model.StarWarsCharacter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;

import java.util.List;

@Service
public class StarWarsVectorStoreService implements CommandLineRunner {

    private final StarWarsCharacterRepository repository;
    private final SimpleVectorStore store;

    public StarWarsVectorStoreService(StarWarsCharacterRepository repository, SimpleVectorStore store) {
        this.repository = repository;
        this.store = store;
    }

    public void indexCharacters() {

        List<Document> documents = repository.findAll()
                .stream()
                .map(this::toDocument)
                .toList();

        store.add(documents);
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

        return new Document(content);
    }

    @Override
    public void run(String... args) {
        indexCharacters();

        System.out.println(
                "Indicizzati " + repository.count() + " personaggi Star Wars"

        );
    }
}
