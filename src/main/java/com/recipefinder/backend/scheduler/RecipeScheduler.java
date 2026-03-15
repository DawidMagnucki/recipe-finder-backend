package com.recipefinder.backend.scheduler;

import com.recipefinder.backend.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeScheduler {

    private final RecipeService recipeService;

    @Scheduled(cron = "0 0 6 * * *")
    public void dailyRecipePlan() {
        System.out.println("Executing scheduled daily menu generation at 06:00...");

        recipeService.generateDailyMenu();

        System.out.println("Your daily menu has been generated and saved to the database!");
    }
}
