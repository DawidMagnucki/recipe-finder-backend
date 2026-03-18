package com.recipefinder.backend.mapper;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.dto.FavoriteDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FavoriteMapper {

    public FavoriteDto mapToFavoriteDto(final Favorite favorite) {
        Long recipeId = favorite.getRecipe() != null ? favorite.getRecipe().getId() : favorite.getRecipeIdSnapshot();
        String recipeTitle = favorite.getRecipe() != null ? favorite.getRecipe().getTitle() : favorite.getRecipeTitle();
        String recipeCategory = favorite.getRecipe() != null ? favorite.getRecipe().getCategory() : favorite.getRecipeCategory();
        Integer recipeCalories = favorite.getRecipe() != null ? favorite.getRecipe().getCalories() : favorite.getRecipeCalories();
        String recipeImageUrl = favorite.getRecipe() != null ? favorite.getRecipe().getImageUrl() : favorite.getRecipeImageUrl();

        return FavoriteDto.builder()
                .id(favorite.getId())
                .recipeId(recipeId)
                .recipeTitle(recipeTitle)
                .recipeCategory(recipeCategory)
                .recipeCalories(recipeCalories)
                .recipeImageUrl(recipeImageUrl)
                .build();
    }

    public List<FavoriteDto> mapToFavoriteDtoList(final List<Favorite> favoriteList) {
        return favoriteList.stream()
                .map(this::mapToFavoriteDto)
                .collect(Collectors.toList());
    }
}
