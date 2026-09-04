package com.example.spring_ai_demo;

import com.example.spring_ai_demo.model.StarWarsCharacter;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface StarWarsCharacterRepository extends ListCrudRepository<StarWarsCharacter, Long> {

    Optional<StarWarsCharacter> findByName(String name);
    List<StarWarsCharacter> findByHomeworld(String homeworld);
}
