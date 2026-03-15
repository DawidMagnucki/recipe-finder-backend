package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @PostMapping("/random")
    public Recipe addRandomRecipe() {
        return recipeService.fetchAndSaveRandomRecipe();
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        recipeRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Recipe updateRecipeTitle(@PathVariable Long id, @RequestParam String newTitle) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow();
        recipe.setTitle(newTitle);
        return recipeRepository.save(recipe);
    }
}