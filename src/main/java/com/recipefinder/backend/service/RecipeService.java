package com.recipefinder.backend.service;

import com.recipefinder.backend.client.EdamamClient;
import com.recipefinder.backend.client.TheMealDBClient;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final TheMealDBClient theMealDBClient;
    private final RecipeRepository recipeRepository;
    private final EdamamClient edamamClient;

    public Recipe fetchAndSaveRandomRecipe() {
        Map<String, String> rawMeal = theMealDBClient.fetchRandomMeal();
        if (rawMeal == null) return null;

        return saveToDatabase(rawMeal, rawMeal.get("strCategory"));
    }

    public void generateDailyMenu() {
        saveMealPlan("Breakfast", "Breakfast");
        saveMealPlan("Lunch", "Seafood");
        saveMealPlan("Dinner", "Side");
    }

    private void saveMealPlan(String mealTime, String apiCategory) {
        Map<String, String> rawMeal = theMealDBClient.fetchRandomMealByCategory(apiCategory);
        if (rawMeal != null) {
            saveToDatabase(rawMeal, mealTime);
        }
    }

    private Recipe saveToDatabase(Map<String, String> rawMeal, String categoryName) {
        String title = rawMeal.get("strMeal");
        Integer kcal = edamamClient.getCaloriesForIngredient(title);

        Recipe recipe = Recipe.builder()
                .externalId(rawMeal.get("idMeal"))
                .title(title)
                .instructions(rawMeal.get("strInstructions"))
                .imageUrl(rawMeal.get("strMealThumb"))
                .category(categoryName)
                .calories(kcal)
                .build();

        return recipeRepository.save(recipe);
    }

    public void deleteAllRecipes() {
        recipeRepository.deleteAll();
    }

}