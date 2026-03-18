package com.recipefinder.backend.service;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.ShoppingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingServiceTest {

    @Mock
    private ShoppingRepository shoppingRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ShoppingService shoppingService;

    @Test
    void shouldAddEachRecipeIngredientAsSeparateShoppingItem() {
        Recipe recipe = Recipe.builder()
                .title("Chicken Curry")
                .ingredients("1 cup rice\n2 chicken breasts\n1 tbsp curry paste")
                .build();

        int addedItems = shoppingService.addRecipeIngredients(recipe);

        ArgumentCaptor<ShoppingItem> shoppingItemCaptor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingRepository, times(3)).save(shoppingItemCaptor.capture());

        List<ShoppingItem> savedItems = shoppingItemCaptor.getAllValues();
        assertEquals(3, addedItems);
        assertEquals("Rice", savedItems.get(0).getIngredientName());
        assertEquals("1", savedItems.get(0).getAmount());
        assertEquals("cup", savedItems.get(0).getUnit());
        assertEquals("Chicken Breasts", savedItems.get(1).getIngredientName());
        assertEquals("2", savedItems.get(1).getAmount());
        assertEquals("item", savedItems.get(1).getUnit());
        assertEquals("Curry Paste", savedItems.get(2).getIngredientName());
        assertEquals("1", savedItems.get(2).getAmount());
        assertEquals("tbsp", savedItems.get(2).getUnit());
    }

    @Test
    void shouldAddFavoriteIngredientSnapshotsAsSeparateShoppingItems() {
        Favorite favorite = Favorite.builder()
                .recipeTitle("Soup")
                .recipeIngredients("250 ml broth\n1 carrot")
                .build();

        int addedItems = shoppingService.addFavoriteIngredients(favorite);

        ArgumentCaptor<ShoppingItem> shoppingItemCaptor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingRepository, times(2)).save(shoppingItemCaptor.capture());

        List<ShoppingItem> savedItems = shoppingItemCaptor.getAllValues();
        assertEquals(2, addedItems);
        assertEquals("Broth", savedItems.get(0).getIngredientName());
        assertEquals("250", savedItems.get(0).getAmount());
        assertEquals("ml", savedItems.get(0).getUnit());
        assertEquals("Carrot", savedItems.get(1).getIngredientName());
        assertEquals("1", savedItems.get(1).getAmount());
        assertEquals("item", savedItems.get(1).getUnit());
    }

    @Test
    void shouldParseCombinedQuantityAndUnitAtBeginningOfIngredientLine() {
        List<ShoppingService.IngredientEntry> entries = shoppingService.parseIngredients("200ml milk\n1600g flour\n3 tsp baking powder");

        assertEquals("Milk", entries.get(0).ingredientName());
        assertEquals("200", entries.get(0).amount());
        assertEquals("ml", entries.get(0).unit());

        assertEquals("Flour", entries.get(1).ingredientName());
        assertEquals("1600", entries.get(1).amount());
        assertEquals("g", entries.get(1).unit());

        assertEquals("Baking Powder", entries.get(2).ingredientName());
        assertEquals("3", entries.get(2).amount());
        assertEquals("tsp", entries.get(2).unit());
    }

    @Test
    void shouldMergeCompatibleUnitsForSameIngredient() {
        List<ShoppingItem> items = new ArrayList<>();
        items.add(ShoppingItem.builder().ingredientName("Milk").amount("200").unit("ml").isPurchased(false).build());
        items.add(ShoppingItem.builder().ingredientName("Milk").amount("1").unit("l").isPurchased(false).build());
        items.add(ShoppingItem.builder().ingredientName("Milk").amount("300").unit("ml").isPurchased(false).build());
        when(shoppingRepository.findAll()).thenReturn(items);

        int mergedCount = shoppingService.mergeAndRecalculateItems();

        ArgumentCaptor<ShoppingItem> shoppingItemCaptor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingRepository, times(1)).deleteAll();
        verify(shoppingRepository, times(1)).save(shoppingItemCaptor.capture());

        ShoppingItem mergedItem = shoppingItemCaptor.getValue();
        assertEquals(1, mergedCount);
        assertEquals("Milk", mergedItem.getIngredientName());
        assertEquals("1.5", mergedItem.getAmount());
        assertEquals("l", mergedItem.getUnit());
    }
}
