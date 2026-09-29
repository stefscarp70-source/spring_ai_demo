package com.example.spring_ai_demo.tool.cooking.dto;

import java.util.Collections;
import java.util.List;

public record RecipeIngredients(
        String recipeName,
        List<IngredientDto> list,
        String errorMessage
) {
    public static RecipeIngredients oklist(String name, List<IngredientDto>list) {
        return new RecipeIngredients(name, list, null);
    }

    public static RecipeIngredients warn(String name, String error) {
        return new RecipeIngredients(name, Collections.emptyList(), error);
    }
}
