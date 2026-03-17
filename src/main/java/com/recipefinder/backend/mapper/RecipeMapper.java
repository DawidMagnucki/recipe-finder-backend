package com.recipefinder.backend.mapper;

import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.dto.RecipeDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    public RecipeDto mapToRecipeDto(final Recipe recipe) {
        return RecipeDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .category(recipe.getCategory())
                .calories(recipe.getCalories())
                .imageUrl(recipe.getImageUrl())
                .build();
    }

    public List<RecipeDto> mapToRecipeDtoList(final List<Recipe> recipeList) {
        return recipeList.stream()
                .map(this::mapToRecipeDto)
                .collect(Collectors.toList());
    }
}
