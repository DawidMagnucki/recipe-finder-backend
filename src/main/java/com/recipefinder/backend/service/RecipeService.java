package com.recipefinder.backend.service;

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
        Recipe recipe = Recipe.builder()
                .externalId(rawMeal.get("idMeal"))
                .title(rawMeal.get("strMeal"))
                .instructions(rawMeal.get("strInstructions"))
                .imageUrl(rawMeal.get("strMealThumb"))
                .category(categoryName)
                .build();

        return recipeRepository.save(recipe);
    }
}