package com.recipefinder.backend;

import com.recipefinder.backend.service.RecipeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecipeFinderBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipeFinderBackendApplication.class, args);
	}

	@Bean
	@org.springframework.context.annotation.Profile("!test")
	public CommandLineRunner testRecipeSave(RecipeService recipeService) {
		return args -> {
			System.out.println("Fetching and saving recipe...");
			recipeService.fetchAndSaveRandomRecipe();
			System.out.println("Done! Check you MySQL DB.");
		};
	}
}
