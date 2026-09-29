package com.example.spring_ai_demo.tool.dto;

import com.example.spring_ai_demo.tool.cooking.dto.IngredientDto;

import java.util.List;

public record ToolInvocation(
        String name,
        List<IngredientDto> ingredientDtos,
        String args
) {
    public static ToolInvocation of (String name, List<IngredientDto>ingr) {
        return new ToolInvocation(name, ingr, null);
    }
}
