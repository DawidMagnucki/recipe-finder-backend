package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.repository.ShoppingRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.RecipeService;
import com.recipefinder.backend.service.ShoppingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewSmokeTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ShoppingRepository shoppingRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ShoppingService shoppingService;

    @Test
    void shouldCreateRecipeView() {
        when(recipeRepository.findAll()).thenReturn(List.of(
                Recipe.builder().id(1L).title("Pasta").category("Dinner").calories(600).imageUrl("img").ingredients("1 cup milk").build()
        ));

        RecipeView view = new RecipeView(recipeService, recipeRepository, favoriteRepository, auditService, shoppingService);

        assertNotNull(view);
        assertTrue(containsGrid(view));
    }

    @Test
    void shouldCreateFavoritesView() {
        when(favoriteRepository.findAll()).thenReturn(List.of(
                Favorite.builder().id(1L).recipeTitle("Soup").recipeCategory("Lunch").recipeCalories(250).recipeImageUrl("img").build()
        ));

        FavoritesView view = new FavoritesView(favoriteRepository, auditService, shoppingService);

        assertNotNull(view);
        assertTrue(containsGrid(view));
    }

    @Test
    void shouldCreateShoppingListView() {
        when(recipeRepository.findAll()).thenReturn(List.of(
                Recipe.builder().id(1L).ingredients("1 cup milk").build()
        ));
        when(favoriteRepository.findAll()).thenReturn(List.of(
                Favorite.builder().id(1L).recipeIngredients("2 tsp sugar").build()
        ));
        when(shoppingRepository.findAll()).thenReturn(List.of(
                ShoppingItem.builder().id(1L).ingredientName("Milk").amount("200").unit("ml").isPurchased(false).build()
        ));

        ShoppingListView view = new ShoppingListView(shoppingRepository, recipeRepository, favoriteRepository, shoppingService);

        assertNotNull(view);
        assertTrue(containsGrid(view));
    }
    private boolean containsGrid(Component component) {
        if (component instanceof Grid<?>) {
            return true;
        }
        if (!component.getChildren().toList().isEmpty()) {
            for (Component child : component.getChildren().toList()) {
                if (containsGrid(child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
