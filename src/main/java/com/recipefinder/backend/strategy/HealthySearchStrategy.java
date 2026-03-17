package com.recipefinder.backend.strategy;

import com.recipefinder.backend.domain.Recipe;
import java.util.List;
import java.util.stream.Collectors;

public class HealthySearchStrategy implements RecipeSearchStrategy {
    @Override
    public List<Recipe> filter(List<Recipe> recipes, String query) {
        try {
            int maxKcal = Integer.parseInt(query);
            return recipes.stream()
                    .filter(r -> r.getCalories() != null && r.getCalories() <= maxKcal)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return List.of();
        }
    }
}
