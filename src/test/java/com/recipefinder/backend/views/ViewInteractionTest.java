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
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewInteractionTest {

    @Mock
    private RecipeService recipeService;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private ShoppingRepository shoppingRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private ShoppingService shoppingService;

    @Test
    void shouldCreateRecipeViewActionButtons() throws Exception {
        when(recipeRepository.findAll()).thenReturn(List.of());
        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Pasta")
                .category("Dinner")
                .calories(500)
                .ingredients("1 cup milk")
                .build();

        RecipeView view = new RecipeView(recipeService, recipeRepository, favoriteRepository, auditService, shoppingService);
        HorizontalLayout actions = invokeCreateActions(view, recipe);
        List<Button> buttons = buttons(actions);

        assertFalse(buttons.isEmpty());
        assertFalse(buttons.size() < 3);
    }

    @Test
    void shouldCreateFavoritesViewActionButtons() throws Exception {
        Favorite favorite = Favorite.builder()
                .id(1L)
                .recipeTitle("Soup")
                .recipeCategory("Lunch")
                .recipeCalories(250)
                .recipeIngredients("1 tsp salt")
                .build();
        when(favoriteRepository.findAll()).thenReturn(List.of(favorite));

        FavoritesView view = new FavoritesView(favoriteRepository, auditService, shoppingService);
        HorizontalLayout actions = invokeFavoriteActions(view, favorite);
        List<Button> buttons = buttons(actions);

        assertFalse(buttons.isEmpty());
        assertFalse(buttons.size() < 2);
    }

    @Test
    void shouldRenderShoppingListControls() {
        when(recipeRepository.findAll()).thenReturn(List.of(
                Recipe.builder().ingredients("1 cup milk").build()
        ));
        when(favoriteRepository.findAll()).thenReturn(List.of(
                Favorite.builder().recipeIngredients("2 tsp sugar").build()
        ));
        when(shoppingRepository.findAll()).thenReturn(List.of(
                ShoppingItem.builder().ingredientName("Milk").amount("200").unit("ml").isPurchased(false).build()
        ));
        when(shoppingService.collectIngredientSuggestions(any(), any(), any())).thenReturn(List.of("Milk", "Sugar"));
        when(shoppingService.collectUnitSuggestions(any(), any(), any())).thenReturn(List.of("ml", "tsp"));

        ShoppingListView view = new ShoppingListView(shoppingRepository, recipeRepository, favoriteRepository, shoppingService);

        ComboBox<String> ingredientBox = findComponent(view, ComboBox.class, 0);
        TextField amountField = findComponent(view, TextField.class, 0);
        ComboBox<String> unitBox = findComponent(view, ComboBox.class, 1);
        List<Button> buttons = buttons(view);

        assertNotNull(ingredientBox);
        assertNotNull(amountField);
        assertNotNull(unitBox);
        assertFalse(buttons.stream().noneMatch(button -> "Add".equals(button.getText())));
        assertFalse(buttons.stream().noneMatch(button -> "Recalculate & Merge".equals(button.getText())));
        assertFalse(buttons.stream().noneMatch(button -> "Delete All".equals(button.getText())));
    }

    @Test
    void shouldRenderRecipeDetailsView() throws Exception {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Toast")
                .category("Breakfast")
                .calories(250)
                .imageUrl("img")
                .instructions("Toast bread")
                .ingredients("2 slices bread")
                .build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(favoriteRepository.findByRecipeId(1L)).thenReturn(Optional.empty());

        RecipeDetailsView view = new RecipeDetailsView(recipeRepository, favoriteRepository, auditService, shoppingService);
        invokeRenderView(view, "recipe", "1");

        List<Button> buttons = buttons(view);
        assertFalse(view.getChildren().toList().isEmpty());
        assertFalse(buttons.stream().noneMatch(button -> "Add to Favorites".equals(button.getText())));
        assertFalse(buttons.stream().noneMatch(button -> "Add Ingredients to Shopping List".equals(button.getText())));
    }

    @Test
    void shouldRenderEmptyStateForMissingRecipeId() throws Exception {
        RecipeDetailsView view = new RecipeDetailsView(recipeRepository, favoriteRepository, auditService, shoppingService);

        invokeRenderView(view, "recipe", null);

        H2 heading = findComponent(view, H2.class, 0);
        assertEquals("Recipe details are unavailable.", heading.getText());
    }

    @Test
    void shouldRenderEmptyStateForInvalidRecipeId() throws Exception {
        RecipeDetailsView view = new RecipeDetailsView(recipeRepository, favoriteRepository, auditService, shoppingService);

        invokeRenderView(view, "recipe", "abc");

        H2 heading = findComponent(view, H2.class, 0);
        assertEquals("Invalid recipe identifier.", heading.getText());
    }

    @Test
    void shouldRenderDetachedFavoriteDetailsView() throws Exception {
        Favorite favorite = Favorite.builder()
                .id(2L)
                .recipeTitle("Brownie")
                .recipeCategory("Dessert")
                .recipeCalories(420)
                .recipeImageUrl("img")
                .recipeInstructions("Bake it")
                .recipeIngredients("2 eggs")
                .build();
        when(favoriteRepository.findById(2L)).thenReturn(Optional.of(favorite));

        RecipeDetailsView view = new RecipeDetailsView(recipeRepository, favoriteRepository, auditService, shoppingService);
        invokeRenderView(view, "favorite", "2");

        List<Button> buttons = buttons(view);
        assertFalse(buttons.stream().noneMatch(button -> "Saved in Favorites".equals(button.getText())));
        assertFalse(buttons.stream().noneMatch(button -> "Add Ingredients to Shopping List".equals(button.getText())));
    }

    @Test
    void shouldRenderRemoveFromFavoritesButtonForLinkedRecipe() throws Exception {
        Recipe recipe = Recipe.builder()
                .id(3L)
                .title("Soup")
                .category("Lunch")
                .calories(320)
                .imageUrl("img")
                .instructions("Boil")
                .ingredients("1 carrot")
                .build();
        Favorite linkedFavorite = Favorite.builder()
                .id(5L)
                .recipe(recipe)
                .recipeTitle("Soup")
                .build();
        when(recipeRepository.findById(3L)).thenReturn(Optional.of(recipe));
        when(favoriteRepository.findByRecipeId(3L)).thenReturn(Optional.of(linkedFavorite));

        RecipeDetailsView view = new RecipeDetailsView(recipeRepository, favoriteRepository, auditService, shoppingService);
        invokeRenderView(view, "recipe", "3");

        List<Button> buttons = buttons(view);
        assertFalse(buttons.stream().noneMatch(button -> "Remove from Favorites".equals(button.getText())));
    }

    private HorizontalLayout invokeCreateActions(RecipeView view, Recipe recipe) throws Exception {
        Method method = RecipeView.class.getDeclaredMethod("createActions", Recipe.class);
        method.setAccessible(true);
        return (HorizontalLayout) method.invoke(view, recipe);
    }

    private HorizontalLayout invokeFavoriteActions(FavoritesView view, Favorite favorite) throws Exception {
        Method method = FavoritesView.class.getDeclaredMethod("createActions", Favorite.class);
        method.setAccessible(true);
        return (HorizontalLayout) method.invoke(view, favorite);
    }

    private void invokeRenderView(RecipeDetailsView view, String type, String id) throws Exception {
        Method method = RecipeDetailsView.class.getDeclaredMethod("renderView", String.class, String.class);
        method.setAccessible(true);
        method.invoke(view, type, id);
    }

    private List<Button> buttons(Component root) {
        List<Button> buttons = new ArrayList<>();
        collectButtons(root, buttons);
        return buttons;
    }

    private void collectButtons(Component component, List<Button> buttons) {
        if (component instanceof Button button) {
            buttons.add(button);
        }
        component.getChildren().forEach(child -> collectButtons(child, buttons));
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> T findComponent(Component root, Class<T> type, int index) {
        List<T> matches = new ArrayList<>();
        collectComponents(root, type, matches);
        assertFalse(matches.isEmpty());
        return matches.get(index);
    }

    private <T extends Component> void collectComponents(Component component, Class<T> type, List<T> matches) {
        if (type.isInstance(component)) {
            matches.add(type.cast(component));
        }
        component.getChildren().forEach(child -> collectComponents(child, type, matches));
    }
}
