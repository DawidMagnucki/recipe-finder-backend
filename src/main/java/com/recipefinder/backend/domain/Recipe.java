package com.recipefinder.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String externalId;
    private String title;
    @Column(length = 2000)
    private String instructions;
    private String imageUrl;
    private String category;
    private Integer calories;

}
