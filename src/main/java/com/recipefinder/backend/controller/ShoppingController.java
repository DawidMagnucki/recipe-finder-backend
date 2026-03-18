package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.dto.ShoppingItemDto;
import com.recipefinder.backend.exception.ShoppingItemNotFoundException;
import com.recipefinder.backend.mapper.ShoppingMapper;
import com.recipefinder.backend.repository.ShoppingRepository;
import com.recipefinder.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping")
@RequiredArgsConstructor
public class ShoppingController {

    private final ShoppingRepository shoppingRepository;
    private final ShoppingMapper shoppingMapper;
    private final AuditService auditService;

    @GetMapping
    public List<ShoppingItemDto> getItems() {
        return shoppingMapper.mapToShoppingItemDtoList(shoppingRepository.findAll());
    }

    @PostMapping
    public ShoppingItemDto addItem(@RequestBody ShoppingItemDto itemDto) {
        ShoppingItem item = ShoppingItem.builder()
                .ingredientName(itemDto.getIngredientName())
                .amount(itemDto.getAmount())
                .unit(itemDto.getUnit())
                .isPurchased(itemDto.isPurchased())
                .build();

        ShoppingItem saved = shoppingRepository.save(item);
        auditService.log("API_ADD_SHOPPING_ITEM", "Added: " + saved.getIngredientName());

        return shoppingMapper.mapToShoppingItemDto(saved);
    }

    @PutMapping("/{id}/toggle")
    public ShoppingItemDto togglePurchased(@PathVariable Long id) throws ShoppingItemNotFoundException {
        ShoppingItem item = shoppingRepository.findById(id)
                .orElseThrow(() -> new ShoppingItemNotFoundException(id));

        item.setPurchased(!item.isPurchased());

        ShoppingItem saved = shoppingRepository.save(item);
        auditService.log("API_TOGGLE_SHOPPING_ITEM", "Toggled status for: " + saved.getIngredientName());

        return shoppingMapper.mapToShoppingItemDto(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) throws ShoppingItemNotFoundException {
        if (!shoppingRepository.existsById(id)) {
            throw new ShoppingItemNotFoundException(id);
        }
        shoppingRepository.deleteById(id);
        auditService.log("API_DELETE_SHOPPING_ITEM", "Deleted item ID: " + id);
    }
}
