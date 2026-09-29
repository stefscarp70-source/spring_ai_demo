package com.example.spring_ai_demo.tool.cooking.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record FridgeAvailability(
        boolean available,
        List<IngredientDto> missing_list,
        String errorMessage
) {
    public static FridgeAvailability ok () {
        return new FridgeAvailability(true, Collections.emptyList(), null);
    }
    public static FridgeAvailability missing (String name, Integer q, String unit) {
        return new FridgeAvailability(false, Arrays.asList(IngredientDto.of(name, q, unit)), "ERROR: not possible to proceed, missing required ingredient");
    }

    public static FridgeAvailability missing (List<IngredientDto>list) {
        return new FridgeAvailability(false, list, "ERROR: not possible to proceed, missing required ingredient");
    }

    public static FridgeAvailability warn(String mess) {
        return new FridgeAvailability(false, Collections.emptyList(), mess);
    }
}
