package com.recipefinder.backend.mapper;

import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.dto.ShoppingItemDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShoppingMapper {

    public ShoppingItemDto mapToShoppingItemDto(final ShoppingItem item) {
        return ShoppingItemDto.builder()
                .id(item.getId())
                .ingredientName(item.getIngredientName())
                .amount(item.getAmount())
                .unit(item.getUnit())
                .isPurchased(item.isPurchased())
                .build();
    }

    public List<ShoppingItemDto> mapToShoppingItemDtoList(final List<ShoppingItem> itemList) {
        return itemList.stream()
                .map(this::mapToShoppingItemDto)
                .collect(Collectors.toList());
    }
}
