package com.recipefinder.backend.strategy;

import com.recipefinder.backend.domain.Recipe;
import java.util.List;
import java.util.stream.Collectors;

public class NameSearchStrategy implements RecipeSearchStrategy {
    @Override
    public List<Recipe> filter(List<Recipe> recipes, String query) {
        return recipes.stream()
                .filter(r -> r.getTitle() != null &&
                        r.getTitle().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
