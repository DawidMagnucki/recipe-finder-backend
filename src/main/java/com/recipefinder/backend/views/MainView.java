package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.repository.ShoppingRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.RecipeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;
    private final ShoppingRepository shoppingRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;

    private final Grid<Recipe> recipeGrid = new Grid<>(Recipe.class);
    private final Grid<Favorite> favoriteGrid = new Grid<>(Favorite.class);
    private final Grid<ShoppingItem> shoppingGrid = new Grid<>(ShoppingItem.class);

    public MainView(RecipeService recipeService, RecipeRepository recipeRepository,
                    ShoppingRepository shoppingRepository, FavoriteRepository favoriteRepository, AuditService auditService) {
        this.recipeService = recipeService;
        this.recipeRepository = recipeRepository;
        this.shoppingRepository = shoppingRepository;
        this.favoriteRepository = favoriteRepository;
        this.auditService = auditService;

        setAlignItems(Alignment.CENTER);

        // --- 1. RECIPE SECTION ---
        add(new H1("Recipe Finder Dashboard"));

        recipeGrid.setColumns("title", "category", "calories");

        recipeGrid.addComponentColumn(recipe -> {
            Image image = new Image(recipe.getImageUrl() != null ? recipe.getImageUrl() : "", "No image");
            image.setWidth("60px");
            return image;
        }).setHeader("Preview");

        recipeGrid.addComponentColumn(recipe -> new HorizontalLayout(
                new Button("❤️", e -> {
                    if (!favoriteRepository.existsByRecipeId(recipe.getId())) {
                        favoriteRepository.save(Favorite.builder().recipe(recipe).build());
                        auditService.log("UI_ADD_FAVORITE", "Added recipe: " + recipe.getTitle());
                        refreshFavoriteGrid();
                        Notification.show("Added to Favorites!");
                    } else {
                        Notification.show("Already in Favorites!");
                    }
                }),
                new Button("🛒", e -> {
                    shoppingRepository.save(ShoppingItem.builder()
                            .ingredientName("Ingredients for: " + recipe.getTitle())
                            .amount("1 set")
                            .isPurchased(false).build());
                    auditService.log("UI_ADD_TO_CART", "Recipe: " + recipe.getTitle());
                    refreshShoppingGrid();
                    Notification.show("Added to shopping list!");
                })
        )).setHeader("Actions");

        add(new HorizontalLayout(
                new Button("Generate Daily Menu", e -> {
                    recipeService.generateDailyMenu();
                    auditService.log("UI_GENERATE_MENU", "Manual daily menu generation");
                    refreshGrid();
                }),
                new Button("Fetch Random Recipe", e -> {
                    recipeService.fetchAndSaveRandomRecipe();
                    auditService.log("UI_FETCH_RANDOM", "User fetched random recipe");
                    refreshGrid();
                })
        ), recipeGrid);

        // --- 2. FAVORITES SECTION ---
        add(new H1("My Favorites ❤️"));

        favoriteGrid.setColumns();
        favoriteGrid.addColumn(fav -> fav.getRecipe().getTitle()).setHeader("Recipe Title");
        favoriteGrid.addColumn(fav -> fav.getRecipe().getCategory()).setHeader("Category");
        favoriteGrid.addComponentColumn(fav -> new Button("Remove", e -> {
            String title = fav.getRecipe().getTitle();
            favoriteRepository.delete(fav);
            auditService.log("UI_REMOVE_FAVORITE", "Removed: " + title);
            refreshFavoriteGrid();
        })).setHeader("Remove");

        add(favoriteGrid);

        // --- 3. SHOPPING LIST SECTION ---
        add(new H1("My Shopping List 🛒"));

        TextField ingredientField = new TextField("Ingredient");
        TextField amountField = new TextField("Amount");
        Button addBtn = new Button("Add", e -> {
            if (!ingredientField.isEmpty()) {
                shoppingRepository.save(ShoppingItem.builder()
                        .ingredientName(ingredientField.getValue())
                        .amount(amountField.getValue())
                        .isPurchased(false).build());
                auditService.log("UI_ADD_CUSTOM_ITEM", "Item: " + ingredientField.getValue());
                ingredientField.clear();
                amountField.clear();
                refreshShoppingGrid();
            }
        });
        add(new HorizontalLayout(ingredientField, amountField, addBtn));

        shoppingGrid.setColumns("ingredientName", "amount");
        shoppingGrid.addColumn(item -> item.isPurchased() ? "Yes" : "No").setHeader("Bought?");
        shoppingGrid.addComponentColumn(item -> new Button("❌", e -> {
            String name = item.getIngredientName();
            shoppingRepository.delete(item);
            auditService.log("UI_DELETE_SHOPPING_ITEM", "Deleted: " + name);
            refreshShoppingGrid();
        })).setHeader("Delete");

        add(shoppingGrid);

        refreshGrid();
        refreshShoppingGrid();
        refreshFavoriteGrid();
    }

    private void refreshGrid() {
        recipeGrid.setItems(recipeRepository.findAll());
    }

    private void refreshShoppingGrid() {
        shoppingGrid.setItems(shoppingRepository.findAll());
    }

    private void refreshFavoriteGrid() {
        favoriteGrid.setItems(favoriteRepository.findAll());
    }
}
