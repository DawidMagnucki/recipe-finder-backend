package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.ShoppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shopping")
@RequiredArgsConstructor
public class ShoppingController {

    private final ShoppingRepository shoppingRepository;

    @GetMapping
    public List<ShoppingItem> getItems() {
        return shoppingRepository.findAll();
    }

    @PostMapping
    public ShoppingItem addItem(@RequestBody ShoppingItem item) {
        return shoppingRepository.save(item);
    }

    @PutMapping("/{id}/toggle")
    public ShoppingItem togglePurchased(@PathVariable Long id) {
        ShoppingItem item = shoppingRepository.findById(id).orElseThrow();
        item.setPurchased(!item.isPurchased());
        return shoppingRepository.save(item);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        shoppingRepository.deleteById(id);
    }
}
