package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.dto.FavoriteDto;
import com.recipefinder.backend.dto.RecipeDto;
import com.recipefinder.backend.exception.AlreadyInFavoritesException;
import com.recipefinder.backend.exception.FavoriteNotFoundException;
import com.recipefinder.backend.exception.RecipeNotFoundException;
import com.recipefinder.backend.mapper.FavoriteMapper;
import com.recipefinder.backend.mapper.RecipeMapper;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.RecipeService;
import com.recipefinder.backend.strategy.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;
    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;
    private final RecipeMapper recipeMapper;
    private final FavoriteMapper favoriteMapper;

    @GetMapping
    public List<RecipeDto> getAllRecipes() {
        return recipeMapper.mapToRecipeDtoList(recipeRepository.findAll());
    }

    @PostMapping("/random")
    public RecipeDto addRandomRecipe() {
        Recipe recipe = recipeService.fetchAndSaveRandomRecipe();
        auditService.log("ADD_RANDOM_RECIPE", "Added: " + recipe.getTitle());
        return recipeMapper.mapToRecipeDto(recipe);
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) throws RecipeNotFoundException {
        if (!recipeRepository.existsById(id)) throw new RecipeNotFoundException(id);
        recipeRepository.deleteById(id);
        auditService.log("DELETE_RECIPE", "Deleted recipe with ID: " + id);
    }

    @PutMapping("/{id}")
    public RecipeDto updateRecipeTitle(@PathVariable Long id, @RequestParam String newTitle) throws RecipeNotFoundException {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        String oldTitle = recipe.getTitle();
        recipe.setTitle(newTitle);
        Recipe saved = recipeRepository.save(recipe);
        auditService.log("UPDATE_RECIPE_TITLE", "Changed from '" + oldTitle + "' to '" + newTitle + "'");
        return recipeMapper.mapToRecipeDto(saved);
    }

    @PostMapping("/{id}/favorite")
    public FavoriteDto addToFavorites(@PathVariable Long id) throws RecipeNotFoundException, AlreadyInFavoritesException {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (favoriteRepository.existsByRecipeId(id)) {
            throw new AlreadyInFavoritesException();
        }

        Favorite favorite = favoriteRepository.save(Favorite.builder().recipe(recipe).build());
        auditService.log("ADD_TO_FAVORITES", "Recipe: " + recipe.getTitle());
        return favoriteMapper.mapToFavoriteDto(favorite);
    }

    @GetMapping("/favorites")
    public List<FavoriteDto> getFavorites() {
        return favoriteMapper.mapToFavoriteDtoList(favoriteRepository.findAll());
    }

    @DeleteMapping("/favorites/{id}")
    public void removeFromFavorites(@PathVariable Long id) throws FavoriteNotFoundException {
        if (!favoriteRepository.existsById(id)) {
            throw new FavoriteNotFoundException(id);
        }
        favoriteRepository.deleteById(id);
        auditService.log("REMOVE_FROM_FAVORITES", "Favorite record ID: " + id);
    }


    @GetMapping("/healthy")
    public List<RecipeDto> getHealthyRecipes() {
        List<Recipe> healthy = recipeRepository.findAll().stream()
                .filter(r -> r.getCalories() != null && r.getCalories() < 500)
                .toList();
        return recipeMapper.mapToRecipeDtoList(healthy);
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
    public List<RecipeDto> searchByTitle(@RequestParam String title) {
        List<Recipe> searchResults = recipeRepository.findAll().stream()
                .filter(r -> r.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
        return recipeMapper.mapToRecipeDtoList(searchResults);
    }

    @GetMapping("/category/{name}")
    public List<RecipeDto> getRecipesByCategory(@PathVariable String name) {
        List<Recipe> byCategory = recipeRepository.findAll().stream()
                .filter(r -> r.getCategory().equalsIgnoreCase(name))
                .toList();
        return recipeMapper.mapToRecipeDtoList(byCategory);
    }

    @GetMapping("/search/advanced")
    public List<RecipeDto> advancedSearch(@RequestParam String type, @RequestParam String query) {
        RecipeSearchStrategy strategy;
        if ("healthy".equalsIgnoreCase(type)) {
            strategy = new HealthySearchStrategy();
        } else {
            strategy = new NameSearchStrategy();
        }

        List<Recipe> filtered = strategy.filter(recipeRepository.findAll(), query);
        return recipeMapper.mapToRecipeDtoList(filtered);
    }
    @DeleteMapping("/all")
    public void deleteAll() {
        recipeService.deleteAllRecipes();
        auditService.log("SERVICE_DELETE_ALL", "All recipes removed from database");
    }

}