package com.recipefinder.backend;

import com.recipefinder.backend.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class RecipeFinderBackendApplicationUnitTest {

    @Test
    void shouldDelegateMainToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            RecipeFinderBackendApplication.main(new String[]{"arg"});

            springApplication.verify(() -> SpringApplication.run(RecipeFinderBackendApplication.class, new String[]{"arg"}));
        }
    }

    @Test
    void shouldCreateRunnerThatFetchesRecipe() throws Exception {
        RecipeService recipeService = mock(RecipeService.class);
        RecipeFinderBackendApplication application = new RecipeFinderBackendApplication();

        CommandLineRunner runner = application.testRecipeSave(recipeService);
        runner.run();

        verify(recipeService).fetchAndSaveRandomRecipe();
    }
}
