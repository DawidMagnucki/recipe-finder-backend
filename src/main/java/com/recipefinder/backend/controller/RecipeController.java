package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.recipefinder.backend.strategy.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;
    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @PostMapping("/random")
    public Recipe addRandomRecipe() {
        Recipe recipe = recipeService.fetchAndSaveRandomRecipe();
        auditService.log("ADD_RANDOM_RECIPE", "Added: " + recipe.getTitle());
        return recipe;
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        recipeRepository.deleteById(id);
        auditService.log("DELETE_RECIPE", "Deleted recipe with ID: " + id);
    }

    @PutMapping("/{id}")
    public Recipe updateRecipeTitle(@PathVariable Long id, @RequestParam String newTitle) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow();
        String oldTitle = recipe.getTitle();
        recipe.setTitle(newTitle);
        Recipe saved = recipeRepository.save(recipe);
        auditService.log("UPDATE_RECIPE_TITLE", "Changed from '" + oldTitle + "' to '" + newTitle + "'");
        return saved;
    }

    @PostMapping("/{id}/favorite")
    public Favorite addToFavorites(@PathVariable Long id) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow();
        if (favoriteRepository.existsByRecipeId(id)) {
            throw new RuntimeException("Recipe already in favorites");
        }
        Favorite favorite = favoriteRepository.save(Favorite.builder().recipe(recipe).build());
        auditService.log("ADD_TO_FAVORITES", "Recipe: " + recipe.getTitle());
        return favorite;
    }

    @GetMapping("/favorites")
    public List<Favorite> getFavorites() {
        return favoriteRepository.findAll();
    }

    @DeleteMapping("/favorites/{id}")
    public void removeFromFavorites(@PathVariable Long id) {
        favoriteRepository.deleteById(id);
        auditService.log("REMOVE_FROM_FAVORITES", "Favorite record ID: " + id);
    }

    @GetMapping("/healthy")
    public List<Recipe> getHealthyRecipes() {
        return recipeRepository.findAll().stream()
                .filter(r -> r.getCalories() != null && r.getCalories() < 500)
                .toList();
    }

    @GetMapping("/stats/average-calories")
    public Double getAverageCalories() {
        return recipeRepository.findAll().stream()
                .filter(r -> r.getCalories() != null)
                .mapToDouble(Recipe::getCalories)
                .average()
                .orElse(0.0);
    }

    @GetMapping("/search")
    public List<Recipe> searchByTitle(@RequestParam String title) {
        return recipeRepository.findAll().stream()
                .filter(r -> r.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

    @GetMapping("/category/{name}")
    public List<Recipe> getRecipesByCategory(@PathVariable String name) {
        return recipeRepository.findAll().stream()
                .filter(r -> r.getCategory().equalsIgnoreCase(name))
                .toList();
    }

    @GetMapping("/search/advanced")
    public List<Recipe> advancedSearch(@RequestParam String type, @RequestParam String query) {
        RecipeSearchStrategy strategy;

        if ("healthy".equalsIgnoreCase(type)) {
            strategy = new HealthySearchStrategy();
        } else {
            strategy = new NameSearchStrategy();
        }

        List<Recipe> allRecipes = recipeRepository.findAll();
        return strategy.filter(allRecipes, query);
    }
}