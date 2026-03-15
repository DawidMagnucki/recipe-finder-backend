package com.recipefinder.backend;

import com.recipefinder.backend.service.RecipeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RecipeFinderBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipeFinderBackendApplication.class, args);
	}

	@Bean
	@org.springframework.context.annotation.Profile("!test")
	public CommandLineRunner testRecipeSave(RecipeService recipeService) {
		return args -> {
			System.out.println("Pobieram i zapisuję przepis...");
			recipeService.fetchAndSaveRandomRecipe();
			System.out.println("Zrobione! Sprawdź bazę danych MySQL.");
		};
	}
}