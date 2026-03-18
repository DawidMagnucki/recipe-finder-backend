package com.recipefinder.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDto {
    private Long id;
    private Long recipeId;
    private String recipeTitle;
    private String recipeCategory;
    private Integer recipeCalories;
    private String recipeImageUrl;
}
