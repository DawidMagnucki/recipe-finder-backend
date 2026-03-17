package com.recipefinder.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    private Long id;
    private String title;
    private String category;
    private Integer calories;
    private String imageUrl;
}
