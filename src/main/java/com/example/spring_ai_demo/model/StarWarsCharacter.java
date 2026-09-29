package com.example.spring_ai_demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
//import org.springframework.data.annotation.Id;
import jakarta.persistence.Id;
//import org.springframework.data.relational.core.mapping.Table;

@Table(name="star_wars_character")
@Entity
public record StarWarsCharacter (
    @Id
    Long id,
    String name,
    Double height,
    Double mass,
    String hairColor,
    String skinColor,
    String eyeColor,
    Double birthYear,
    String sex,
    String gender,
    String homeworld,
    String species,
    String films,
    String vehicles,
    String starships
){

}
