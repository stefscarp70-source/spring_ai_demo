package com.example.spring_ai_demo.tool.cooking.dto;

import com.example.spring_ai_demo.db.entity.Ingredient;
import com.example.spring_ai_demo.db.entity.Recipe;

public record IngredientDto(
        String name,
        Integer quantity,
        String unit
        //Double price
) {
    public static IngredientDto of(String name, Integer q, String unit) {
        return new IngredientDto(name, q, unit);
    }

    public static IngredientDto fromFridgeDB(Ingredient ing) {
        return new IngredientDto(ing.getName(), ing.getQ(), ing.getUnit());
    }

    public static IngredientDto fromRecipeDB(Recipe ing, int n) {
        return new IngredientDto(ing.getIngredient(), ing.getQ() * n, ing.getUnit());
    }
}
