package com.recipefinder.backend.service;

import com.recipefinder.backend.client.EdamamClient;
import com.recipefinder.backend.client.TheMealDBClient;
import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final TheMealDBClient theMealDBClient;
    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;
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
                .ingredients(extractIngredients(rawMeal))
                .imageUrl(rawMeal.get("strMealThumb"))
                .category(categoryName)
                .calories(kcal)
                .build();

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteAllRecipes() {
        detachFavoritesFromRecipes();
        recipeRepository.deleteAll();
    }

    @Transactional
    public void deleteRecipe(Recipe recipe) {
        favoriteRepository.findAll().stream()
                .filter(favorite -> favorite.getRecipe() != null && favorite.getRecipe().getId().equals(recipe.getId()))
                .forEach(this::detachFavoriteSnapshot);
        recipeRepository.delete(recipe);
    }

    private void detachFavoritesFromRecipes() {
        favoriteRepository.findAll().stream()
                .filter(favorite -> favorite.getRecipe() != null)
                .forEach(this::detachFavoriteSnapshot);
    }

    private void detachFavoriteSnapshot(Favorite favorite) {
        Recipe recipe = favorite.getRecipe();
        favorite.setRecipeIdSnapshot(recipe.getId());
        favorite.setRecipeTitle(recipe.getTitle());
        favorite.setRecipeCategory(recipe.getCategory());
        favorite.setRecipeCalories(recipe.getCalories());
        favorite.setRecipeImageUrl(recipe.getImageUrl());
        favorite.setRecipeInstructions(recipe.getInstructions());
        favorite.setRecipeIngredients(recipe.getIngredients());
        favorite.setRecipe(null);
        favoriteRepository.save(favorite);
    }

    private String extractIngredients(Map<String, String> rawMeal) {
        List<String> ingredients = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            String ingredient = rawMeal.get("strIngredient" + i);
            if (ingredient == null || ingredient.isBlank()) {
                continue;
            }

            String measure = rawMeal.get("strMeasure" + i);
            String line = (measure != null ? measure.trim() : "");
            if (!line.isEmpty()) {
                line += " ";
            }
            line += ingredient.trim();
            ingredients.add(line.trim());
        }

        return String.join("\n", ingredients);
    }

}
