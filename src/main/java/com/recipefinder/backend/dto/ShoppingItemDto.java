package com.recipefinder.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItemDto {
    private Long id;
    private String ingredientName;
    private String amount;
    private String unit;
    private boolean isPurchased;
}
