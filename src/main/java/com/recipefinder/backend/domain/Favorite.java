package com.recipefinder.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    private Long recipeIdSnapshot;

    private String recipeTitle;

    private String recipeCategory;

    private Integer recipeCalories;

    private String recipeImageUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String recipeInstructions;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String recipeIngredients;
}
