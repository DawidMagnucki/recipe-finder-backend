package com.recipefinder.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.dto.FavoriteDto;
import com.recipefinder.backend.dto.RecipeDto;
import com.recipefinder.backend.exception.AlreadyInFavoritesException;
import com.recipefinder.backend.exception.GlobalHttpErrorHandler;
import com.recipefinder.backend.exception.RecipeNotFoundException;
import com.recipefinder.backend.mapper.FavoriteMapper;
import com.recipefinder.backend.mapper.RecipeMapper;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@Import({RecipeMapper.class, FavoriteMapper.class, GlobalHttpErrorHandler.class})
@ActiveProfiles("test")
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private RecipeRepository recipeRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private RecipeService recipeService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private FavoriteRepository favoriteRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditService auditService;

    @Test
    void shouldReturnAllRecipes() throws Exception {
        Recipe recipe = Recipe.builder().id(1L).title("Pasta").category("Dinner").calories(650).imageUrl("img").build();
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Pasta"))
                .andExpect(jsonPath("$[0].category").value("Dinner"))
                .andExpect(jsonPath("$[0].calories").value(650));
    }

    @Test
    void shouldAddRandomRecipe() throws Exception {
        Recipe recipe = Recipe.builder().id(2L).title("Soup").category("Lunch").calories(320).imageUrl("img").build();
        when(recipeService.fetchAndSaveRandomRecipe()).thenReturn(recipe);

        mockMvc.perform(post("/api/recipes/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Soup"))
                .andExpect(jsonPath("$.category").value("Lunch"));

        verify(auditService).log("ADD_RANDOM_RECIPE", "Added: Soup");
    }

    @Test
    void shouldDeleteRecipe() throws Exception {
        Recipe recipe = Recipe.builder().id(3L).title("Cake").build();
        when(recipeRepository.findById(3L)).thenReturn(Optional.of(recipe));

        mockMvc.perform(delete("/api/recipes/3"))
                .andExpect(status().isOk());

        verify(recipeService).deleteRecipe(recipe);
        verify(auditService).log("DELETE_RECIPE", "Deleted recipe with ID: 3");
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingRecipe() throws Exception {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/recipes/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Recipe with ID 99 was not found in our database."));
    }

    @Test
    void shouldUpdateRecipeTitle() throws Exception {
        Recipe recipe = Recipe.builder().id(4L).title("Old").build();
        Recipe savedRecipe = Recipe.builder().id(4L).title("New").build();
        when(recipeRepository.findById(4L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        mockMvc.perform(put("/api/recipes/4").param("newTitle", "New"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"));

        verify(auditService).log("UPDATE_RECIPE_TITLE", "Changed from 'Old' to 'New'");
    }

    @Test
    void shouldAddRecipeToFavorites() throws Exception {
        Recipe recipe = Recipe.builder()
                .id(5L)
                .title("Burger")
                .category("Dinner")
                .calories(700)
                .imageUrl("img")
                .instructions("Cook")
                .ingredients("1 bun")
                .build();
        Favorite favorite = Favorite.builder()
                .id(7L)
                .recipe(recipe)
                .recipeIdSnapshot(5L)
                .recipeTitle("Burger")
                .recipeCategory("Dinner")
                .recipeCalories(700)
                .recipeImageUrl("img")
                .recipeInstructions("Cook")
                .recipeIngredients("1 bun")
                .build();

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(recipe));
        when(favoriteRepository.existsByRecipeId(5L)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);

        mockMvc.perform(post("/api/recipes/5/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.recipeId").value(5))
                .andExpect(jsonPath("$.recipeTitle").value("Burger"));

        verify(auditService).log("ADD_TO_FAVORITES", "Recipe: Burger");
    }

    @Test
    void shouldReturnBadRequestWhenRecipeAlreadyInFavorites() throws Exception {
        Recipe recipe = Recipe.builder().id(5L).title("Burger").build();
        when(recipeRepository.findById(5L)).thenReturn(Optional.of(recipe));
        when(favoriteRepository.existsByRecipeId(5L)).thenReturn(true);

        mockMvc.perform(post("/api/recipes/5/favorite"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This recipe is already in your favorites list!"));

        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void shouldReturnHealthyRecipes() throws Exception {
        Recipe healthy = Recipe.builder().id(1L).title("Salad").category("Lunch").calories(250).build();
        Recipe unhealthy = Recipe.builder().id(2L).title("Pizza").category("Dinner").calories(800).build();
        when(recipeRepository.findAll()).thenReturn(List.of(healthy, unhealthy));

        mockMvc.perform(get("/api/recipes/healthy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Salad"));
    }

    @Test
    void shouldReturnAverageCalories() throws Exception {
        Recipe first = Recipe.builder().id(1L).calories(200).build();
        Recipe second = Recipe.builder().id(2L).calories(400).build();
        when(recipeRepository.findAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/recipes/stats/average-calories"))
                .andExpect(status().isOk())
                .andExpect(content().string("300.0"));
    }

    @Test
    void shouldSearchRecipesByTitle() throws Exception {
        Recipe recipe = Recipe.builder().id(1L).title("Chicken Curry").category("Dinner").calories(500).build();
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        mockMvc.perform(get("/api/recipes/search").param("title", "curry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Chicken Curry"));
    }

    @Test
    void shouldReturnRecipesByCategory() throws Exception {
        Recipe recipe = Recipe.builder().id(1L).title("Toast").category("Breakfast").calories(220).build();
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        mockMvc.perform(get("/api/recipes/category/Breakfast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Breakfast"));
    }

    @Test
    void shouldUseHealthyStrategyForAdvancedSearch() throws Exception {
        Recipe recipe = Recipe.builder().id(1L).title("Fit Bowl").category("Lunch").calories(350).build();
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        mockMvc.perform(get("/api/recipes/search/advanced")
                        .param("type", "healthy")
                        .param("query", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Fit Bowl"));
    }

    @Test
    void shouldUseNameStrategyForAdvancedSearch() throws Exception {
        Recipe recipe = Recipe.builder().id(1L).title("Apple Pie").category("Dessert").calories(450).build();
        when(recipeRepository.findAll()).thenReturn(List.of(recipe));

        mockMvc.perform(get("/api/recipes/search/advanced")
                        .param("type", "name")
                        .param("query", "apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Apple Pie"));
    }

    @Test
    void shouldDeleteAllRecipes() throws Exception {
        mockMvc.perform(delete("/api/recipes/all"))
                .andExpect(status().isOk());

        verify(recipeService).deleteAllRecipes();
        verify(auditService).log("SERVICE_DELETE_ALL", "All recipes removed from database");
    }
}
