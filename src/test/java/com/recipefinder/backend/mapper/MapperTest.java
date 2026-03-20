package com.recipefinder.backend.mapper;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.dto.FavoriteDto;
import com.recipefinder.backend.dto.RecipeDto;
import com.recipefinder.backend.dto.ShoppingItemDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperTest {

    private final RecipeMapper recipeMapper = new RecipeMapper();
    private final FavoriteMapper favoriteMapper = new FavoriteMapper();
    private final ShoppingMapper shoppingMapper = new ShoppingMapper();

    @Test
    void shouldMapRecipeToDtoAndList() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Pasta")
                .category("Dinner")
                .calories(600)
                .imageUrl("img")
                .build();

        RecipeDto dto = recipeMapper.mapToRecipeDto(recipe);
        List<RecipeDto> list = recipeMapper.mapToRecipeDtoList(List.of(recipe));

        assertEquals("Pasta", dto.getTitle());
        assertEquals("Dinner", dto.getCategory());
        assertEquals(1, list.size());
    }

    @Test
    void shouldMapFavoriteUsingLiveRecipe() {
        Recipe recipe = Recipe.builder()
                .id(5L)
                .title("Soup")
                .category("Lunch")
                .calories(300)
                .imageUrl("img")
                .build();
        Favorite favorite = Favorite.builder().id(9L).recipe(recipe).build();

        FavoriteDto dto = favoriteMapper.mapToFavoriteDto(favorite);

        assertEquals(9L, dto.getId());
        assertEquals(5L, dto.getRecipeId());
        assertEquals("Soup", dto.getRecipeTitle());
        assertEquals("Lunch", dto.getRecipeCategory());
        assertEquals(300, dto.getRecipeCalories());
    }

    @Test
    void shouldMapFavoriteUsingSnapshotWhenRecipeDetached() {
        Favorite favorite = Favorite.builder()
                .id(10L)
                .recipeIdSnapshot(8L)
                .recipeTitle("Cake")
                .recipeCategory("Dessert")
                .recipeCalories(450)
                .recipeImageUrl("img")
                .build();

        FavoriteDto dto = favoriteMapper.mapToFavoriteDto(favorite);

        assertEquals(8L, dto.getRecipeId());
        assertEquals("Cake", dto.getRecipeTitle());
        assertEquals("Dessert", dto.getRecipeCategory());
        assertEquals(450, dto.getRecipeCalories());
        assertEquals(1, favoriteMapper.mapToFavoriteDtoList(List.of(favorite)).size());
    }

    @Test
    void shouldMapShoppingItemToDtoAndList() {
        ShoppingItem item = ShoppingItem.builder()
                .id(4L)
                .ingredientName("Milk")
                .amount("200")
                .unit("ml")
                .isPurchased(true)
                .build();

        ShoppingItemDto dto = shoppingMapper.mapToShoppingItemDto(item);
        List<ShoppingItemDto> list = shoppingMapper.mapToShoppingItemDtoList(List.of(item));

        assertEquals("Milk", dto.getIngredientName());
        assertEquals("200", dto.getAmount());
        assertEquals("ml", dto.getUnit());
        assertEquals(1, list.size());
    }
}
