package com.recipefinder.backend.strategy;

import com.recipefinder.backend.domain.Recipe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrategyTest {

    @Test
    void shouldFilterRecipesByName() {
        NameSearchStrategy strategy = new NameSearchStrategy();
        List<Recipe> recipes = List.of(
                Recipe.builder().title("Chicken Curry").build(),
                Recipe.builder().title("Tomato Soup").build(),
                Recipe.builder().title(null).build()
        );

        List<Recipe> result = strategy.filter(recipes, "curry");

        assertEquals(1, result.size());
        assertEquals("Chicken Curry", result.get(0).getTitle());
    }

    @Test
    void shouldFilterHealthyRecipesByCalories() {
        HealthySearchStrategy strategy = new HealthySearchStrategy();
        List<Recipe> recipes = List.of(
                Recipe.builder().title("Salad").calories(250).build(),
                Recipe.builder().title("Burger").calories(900).build(),
                Recipe.builder().title("Unknown").calories(null).build()
        );

        List<Recipe> result = strategy.filter(recipes, "400");

        assertEquals(1, result.size());
        assertEquals("Salad", result.get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyListForInvalidHealthySearchQuery() {
        HealthySearchStrategy strategy = new HealthySearchStrategy();

        List<Recipe> result = strategy.filter(List.of(Recipe.builder().title("Salad").calories(250).build()), "abc");

        assertEquals(0, result.size());
    }
}
