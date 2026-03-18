package com.recipefinder.backend.service;

import com.recipefinder.backend.client.EdamamClient;
import com.recipefinder.backend.client.TheMealDBClient;
import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private TheMealDBClient theMealDBClient;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private EdamamClient edamamClient;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void shouldDetachFavoritesAndKeepSnapshotsWhenDeletingAllRecipes() {
        Recipe recipe = Recipe.builder()
                .id(10L)
                .title("Pasta")
                .category("Dinner")
                .calories(620)
                .imageUrl("image-1")
                .instructions("Boil pasta")
                .ingredients("200g pasta\n1 tbsp olive oil")
                .build();
        Favorite favorite = Favorite.builder().id(1L).recipe(recipe).build();
        when(favoriteRepository.findAll()).thenReturn(List.of(favorite));

        recipeService.deleteAllRecipes();

        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).save(favoriteCaptor.capture());
        verify(recipeRepository).deleteAll();

        Favorite savedFavorite = favoriteCaptor.getValue();
        assertNull(savedFavorite.getRecipe());
        assertEquals(10L, savedFavorite.getRecipeIdSnapshot());
        assertEquals("Pasta", savedFavorite.getRecipeTitle());
        assertEquals("Dinner", savedFavorite.getRecipeCategory());
        assertEquals(620, savedFavorite.getRecipeCalories());
        assertEquals("image-1", savedFavorite.getRecipeImageUrl());
        assertEquals("Boil pasta", savedFavorite.getRecipeInstructions());
        assertEquals("200g pasta\n1 tbsp olive oil", savedFavorite.getRecipeIngredients());
    }

    @Test
    void shouldDetachMatchingFavoriteWhenDeletingSingleRecipe() {
        Recipe recipe = Recipe.builder()
                .id(22L)
                .title("Soup")
                .category("Lunch")
                .calories(280)
                .imageUrl("image-2")
                .instructions("Simmer")
                .ingredients("2 cups broth\n1 carrot")
                .build();
        Favorite matchingFavorite = Favorite.builder().id(2L).recipe(recipe).build();
        Favorite otherFavorite = Favorite.builder()
                .id(3L)
                .recipe(Recipe.builder().id(99L).title("Cake").category("Dessert").build())
                .build();
        when(favoriteRepository.findAll()).thenReturn(List.of(matchingFavorite, otherFavorite));

        recipeService.deleteRecipe(recipe);

        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository, times(1)).save(favoriteCaptor.capture());
        verify(recipeRepository).delete(recipe);

        Favorite savedFavorite = favoriteCaptor.getValue();
        assertNull(savedFavorite.getRecipe());
        assertEquals(22L, savedFavorite.getRecipeIdSnapshot());
        assertEquals("Soup", savedFavorite.getRecipeTitle());
        assertEquals("Lunch", savedFavorite.getRecipeCategory());
        assertEquals(280, savedFavorite.getRecipeCalories());
        assertEquals("image-2", savedFavorite.getRecipeImageUrl());
        assertEquals("Simmer", savedFavorite.getRecipeInstructions());
        assertEquals("2 cups broth\n1 carrot", savedFavorite.getRecipeIngredients());
    }

    @Test
    void shouldGenerateThreeMealsForDailyMenu() {
        when(theMealDBClient.fetchRandomMealByCategory("Breakfast"))
                .thenReturn(Map.of("idMeal", "1", "strMeal", "Omelette", "strInstructions", "Cook", "strMealThumb", "img-1"));
        when(theMealDBClient.fetchRandomMealByCategory("Seafood"))
                .thenReturn(Map.of("idMeal", "2", "strMeal", "Salmon", "strInstructions", "Bake", "strMealThumb", "img-2"));
        when(theMealDBClient.fetchRandomMealByCategory("Side"))
                .thenReturn(Map.of("idMeal", "3", "strMeal", "Rice", "strInstructions", "Boil", "strMealThumb", "img-3"));
        when(edamamClient.getCaloriesForIngredient(any())).thenReturn(400);

        recipeService.generateDailyMenu();

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository, times(3)).save(recipeCaptor.capture());

        List<Recipe> savedRecipes = recipeCaptor.getAllValues();
        assertEquals("Breakfast", savedRecipes.get(0).getCategory());
        assertEquals("Lunch", savedRecipes.get(1).getCategory());
        assertEquals("Dinner", savedRecipes.get(2).getCategory());
    }
}
