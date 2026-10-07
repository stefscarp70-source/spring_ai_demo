package com.example.spring_ai_demo.tool.cooking;

import com.example.spring_ai_demo.db.FridgeRepository;
import com.example.spring_ai_demo.db.RecipeRepository;
import com.example.spring_ai_demo.db.entity.Ingredient;
import com.example.spring_ai_demo.db.entity.Recipe;
import com.example.spring_ai_demo.tool.cooking.dto.FridgeAvailability;
import com.example.spring_ai_demo.tool.cooking.dto.IngredientDto;
import com.example.spring_ai_demo.tool.cooking.dto.IngredientList;
import com.example.spring_ai_demo.tool.cooking.dto.MarketBag;
import com.example.spring_ai_demo.tool.cooking.dto.RecipeIngredients;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KitchenTools {



    private final static String BUYMARKET = "buyAtMarket";
    private final static String TAKEFRIDGE = "findFridgeMissingIngredients";

    private final FridgeRepository fridgeRepository;
    private final RecipeRepository recipeRepository;

    ObjectMapper mapper = new ObjectMapper();

    public KitchenTools(FridgeRepository fridgeRepository, RecipeRepository recipeRepository) {

        this.fridgeRepository = fridgeRepository;
        this.recipeRepository = recipeRepository;
    }

    @Tool( name = TAKEFRIDGE,
            description= """
            Searches in my fridge whether all the ingredients in the list provided
            are available.
            
            The input parameter 'list' must be a JSON array of Ingredient objects, not a string containing JSON.
                    Each Ingredient contains:
                    - name: ingredient name
                    - quantity: required quantity
                    - unit: measurement unit
                    
            It returns
            available: true or false (about the whole list and enough quantities)
            missing_list: list of the ingredients missing, with each name and quantity missing.
            
            """)
    public FridgeAvailability findFridgeMissingIngredients(
            List<IngredientDto> list
    ) {
        log.info("  Tool findFridgeMissingIngredients: list: {}", list);
        if (list==null) return FridgeAvailability.warn("Invalid null list as ingredients input");

        FridgeAvailability resp;
        List<IngredientDto> ingredientDtos = fridgeList();
        List<IngredientDto> missing = new ArrayList<>();

        for (IngredientDto required : list) {
            Optional<IngredientDto> found = ingredientDtos.stream()
                    .filter(f -> f.name().equalsIgnoreCase(required.name()))
                    .findFirst();

            if (found.isEmpty()) {
                // completamente assente
                missing.add(required);
                continue;
            }

            IngredientDto available = found.get();

            // presente ma quantità insufficiente
            if (available.quantity().compareTo(required.quantity()) < 0) {
                Integer missingQuantity = required.quantity() - available.quantity();
                missing.add(new IngredientDto(
                        required.name(),
                        missingQuantity,
                        required.unit()
                ));
            }
        }

        if (missing.isEmpty()) {
            return FridgeAvailability.ok();
        } else {
            return FridgeAvailability.missing(missing);
        }
    }

    public List<IngredientDto> fridgeList() {
        log.info("  Tool fridgeList: complete list...");

        List<Ingredient> ingredients = fridgeRepository.findAll();
        return ingredients.stream()
                .map(IngredientDto::fromFridgeDB)
                .toList();
    }

    public List<IngredientDto> getFromRecipe(String recipeName, Integer npeople) {
        log.info("  Tool ingredients from recipe...");

        List<Recipe> ingredients = recipeRepository.findAllByName(recipeName);
        return ingredients.stream()
                .map(ing -> IngredientDto.fromRecipeDB(ing, npeople))
                .toList();
    }

    public List<RecipeIngredients> getAllRecipes() {

        Map<String, List<Recipe>> recipes0 = recipeRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(Recipe::getName));
        Map<String, List<IngredientDto>> recipes = recipeRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Recipe::getName,
                        Collectors.mapping(
                                r -> new IngredientDto(
                                        r.getIngredient(),
                                        r.getQ(),
                                        r.getUnit()
                                ),
                                Collectors.toList()
                        )
                ));
        return recipes.entrySet()
                .stream()
                .map(entry -> RecipeIngredients.oklist(entry.getKey(), entry.getValue()))
                .toList();
    }


    @Tool( name = "getRecipeIngredients",
            description= """
            Given a recipe name and the number of persons, it returns the ingredient list required to prepare it
            with each ingredient name and quantity.       
            """)
    public RecipeIngredients getRecipeIngredients(
            String recipeName, Integer number
    ) {
        log.info("  Tool getRecipeIngredients: recipe name: {} for {} persons", recipeName, number);
        if (recipeName==null) return RecipeIngredients.warn(recipeName, "Invalid null list as ingredient input");
        if (number==null) return RecipeIngredients.warn(recipeName, "Invalid null number of persons as recipe input");

        /*
        if (recipeName.equals("minestrone")) {
            List<IngredientDto> ingredientDtos = Arrays.asList(
                    IngredientDto.of("stelline", 100, "g"),
                    IngredientDto.of("verdure", 100, "g")
            );
            return RecipeIngredients.oklist(recipeName, ingredientDtos);
        }
        if (recipeName.equals("carbonara")) {
            List<IngredientDto> ingredientDtos = Arrays.asList(
                    IngredientDto.of(PENNE, 100, "g"),
                    IngredientDto.of(GUANCIALE, 100, "g")
            );
            return RecipeIngredients.oklist(recipeName, ingredientDtos);
        }

         */

        List<IngredientDto> ingredientDtos = getFromRecipe(recipeName, number);
        return RecipeIngredients.oklist(recipeName, ingredientDtos);
    }



    @Tool( name = BUYMARKET,
            description= """
            Buy at market the missing ingredients in order to have all is required.
            It returns the market bag with the ingredient list of what you bought
            and the total_price spent.       
            """)
    public MarketBag buyAtMarket(
            List<IngredientDto>list
    ) {
        log.info("  Ingredients to buy: {} ", list);
        if (list==null) return MarketBag.warn("Invalid null ingredients list as input");

        if (!list.isEmpty()) {
            return MarketBag.bought(list, 24.5);
        }
        return MarketBag.empty("Nothing bought, nothing spent");
    }

    /*@Tool( name = "cookRecipe",
            description= """
            Proceed cooking the recipe with ingredient list. It returns cooking result.       
            """)*/
    public String cookRecipe(
            String recipeName, List<IngredientDto> ingredientDtos
    ) {
        log.info("  Tool cookRecipe: recipe name: {} ", recipeName);
        if (recipeName==null) return "Invalid null list as ingredient input";
        if (ingredientDtos ==null) return "Invalid null ingredients list as input";

        return "Recipe "+recipeName+"  cooked successfully";
    }

    public List<IngredientDto> extractArgsFromTools(String toolName, String args ) {
        if (toolName.equals(BUYMARKET) || toolName.equals(TAKEFRIDGE)) {
            IngredientList result = mapper.readValue(args, IngredientList.class);

            return result.list();
        }

        return Collections.emptyList();
    }

    /**
     * Operator to extract ingredients from Fridge
     */
    public void extractFromFridge(
            List<IngredientDto>list
    ) {
        log.info("  Ingredients to extract: {} ", list);

        for(IngredientDto dto: list) {
            Optional<Ingredient> opIngredient = fridgeRepository.findByName(dto.name());
            if (opIngredient.isEmpty()) {
                log.warn("  Ingredient {} NOt found, skipping extraction", dto.name());
                continue;
            }
            Ingredient restocking = opIngredient.get();
            restocking.extract(dto.quantity());
            fridgeRepository.save(restocking);
            log.info("    extracting {} >> {}.", dto.name(), dto.quantity());
        }

    }

    /**
     * Operator to restock ingredients into Fridge
     */
    @Transactional
    public void restock(
            List<IngredientDto>list
    ) {
        log.info("  Ingredients to refurnish: {} ", list);

        for(IngredientDto dto: list) {
            Optional<Ingredient> opIngredient = fridgeRepository.findByName(dto.name());
            Ingredient restocking = opIngredient.orElseGet(() -> Ingredient.fromScratch(dto));
            restocking.restock(dto.quantity());
            fridgeRepository.save(restocking);
            log.info("    restocking {} >> {}.", dto.name(), dto.quantity());
        }

    }
}
