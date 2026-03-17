package com.recipefinder.backend.mapper;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.dto.FavoriteDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FavoriteMapper {

    public FavoriteDto mapToFavoriteDto(final Favorite favorite) {
        return FavoriteDto.builder()
                .id(favorite.getId())
                .recipeId(favorite.getRecipe().getId())
                .recipeTitle(favorite.getRecipe().getTitle())
                .build();
    }

    public List<FavoriteDto> mapToFavoriteDtoList(final List<Favorite> favoriteList) {
        return favoriteList.stream()
                .map(this::mapToFavoriteDto)
                .collect(Collectors.toList());
    }
}
