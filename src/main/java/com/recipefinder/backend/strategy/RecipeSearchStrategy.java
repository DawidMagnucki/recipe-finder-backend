package com.recipefinder.backend.strategy;

import com.recipefinder.backend.domain.Recipe;
import java.util.List;

public interface RecipeSearchStrategy {
    List<Recipe> filter(List<Recipe> recipes, String query);
}
