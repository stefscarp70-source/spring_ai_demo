package com.example.spring_ai_demo.tool.cooking.dto;

import java.util.Collections;
import java.util.List;

public record MarketBag(
        List<IngredientDto> ingredientDtos,
        Double total_spent,
        String error_message
) {

    public static MarketBag warn(String err) {
        return new MarketBag(Collections.emptyList(), null, err);
    }

    public static MarketBag bought(List<IngredientDto>list, Double cost) {
        return new MarketBag(list, cost, null);
    }

    public static MarketBag empty(String mess) {
        return new MarketBag(Collections.emptyList(), 0.0, mess);
    }
}
