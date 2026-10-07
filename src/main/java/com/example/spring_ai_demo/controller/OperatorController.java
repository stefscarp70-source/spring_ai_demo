package com.example.spring_ai_demo.controller;

import com.example.spring_ai_demo.tool.cooking.KitchenTools;
import com.example.spring_ai_demo.tool.cooking.dto.IngredientDto;
import com.example.spring_ai_demo.tool.cooking.dto.RecipeIngredients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/api/tool")
@RestController
public class OperatorController {

    private final KitchenTools tools;

    public OperatorController(KitchenTools tools) {
        this.tools = tools;
    }


    @PostMapping("/restock")
    public ResponseEntity<String> restockOperator(@RequestBody List<IngredientDto> ingredientDtos) {
        tools.restock(ingredientDtos);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\": \"Restock successful\"}");
    }

    @PostMapping("/extract")
    public ResponseEntity<String> extractOperator(@RequestBody List<IngredientDto> ingredientDtos) {
        tools.extractFromFridge(ingredientDtos);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\": \"Extraction successful\"}");
    }

    @GetMapping("/fridge")
    public ResponseEntity<List<IngredientDto>> fridgeList() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(tools.fridgeList());
    }

    @GetMapping("/recipe")
    public ResponseEntity<List<IngredientDto>> recipeList(@RequestParam String name, @RequestParam(required = false) Integer n) {
        Integer npeople = n!=null? n : 4;

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(tools.getFromRecipe(name, npeople));
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<RecipeIngredients>> recipeList() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(tools.getAllRecipes());
    }


}
