package com.recipefinder.backend.scheduler;

import com.recipefinder.backend.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecipeSchedulerTest {

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private RecipeScheduler recipeScheduler;

    @Test
    void shouldGenerateDailyRecipePlan() {
        recipeScheduler.dailyRecipePlan();

        verify(recipeService).generateDailyMenu();
    }

    @Test
    void shouldFetchRandomRecipeOnSchedule() {
        recipeScheduler.fetchRandomRecipeEveryFifteenMinutes();

        verify(recipeService).fetchAndSaveRandomRecipe();
    }
}
