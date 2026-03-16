package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.repository.ShoppingRepository;
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

    private final Grid<Recipe> grid = new Grid<>(Recipe.class);
    private final Grid<ShoppingItem> shoppingGrid = new Grid<>();

    public MainView(RecipeService recipeService, RecipeRepository recipeRepository, ShoppingRepository shoppingRepository) {
        this.recipeService = recipeService;
        this.recipeRepository = recipeRepository;
        this.shoppingRepository = shoppingRepository;

        setAlignItems(Alignment.CENTER);
        add(new H1("Recipe Finder Dashboard"));

        // 1. RECIPE SECTION
        grid.setColumns("title", "category");
        grid.addComponentColumn(recipe -> {
            Image image = new Image(recipe.getImageUrl(), "No image");
            image.setWidth("80px");
            return image;
        }).setHeader("Preview");

        grid.addComponentColumn(recipe -> new Button("Add to Shopping List", e -> {
            shoppingRepository.save(ShoppingItem.builder()
                    .ingredientName("Ingredients for: " + recipe.getTitle())
                    .amount("1 set")
                    .isPurchased(false)
                    .build());
            refreshShoppingGrid();
            Notification.show("Added to list!");
        })).setHeader("Actions");

        HorizontalLayout recipeButtons = new HorizontalLayout(
                new Button("Generate Daily Menu Now", e -> {
                    recipeService.generateDailyMenu();
                    refreshGrid();
                }),
                new Button("Fetch Random Recipe", e -> {
                    recipeService.fetchAndSaveRandomRecipe();
                    refreshGrid();
                })
        );

        add(recipeButtons, grid);

        // 2. SHOPPING LIST SECTION
        add(new H1("My Shopping List"));

        TextField ingredientField = new TextField("Ingredient");
        TextField amountField = new TextField("Amount");
        Button addBtn = new Button("Add", e -> {
            if (!ingredientField.isEmpty()) {
                shoppingRepository.save(ShoppingItem.builder()
                        .ingredientName(ingredientField.getValue())
                        .amount(amountField.getValue())
                        .isPurchased(false)
                        .build());
                ingredientField.clear();
                amountField.clear();
                refreshShoppingGrid();
            }
        });

        HorizontalLayout shoppingForm = new HorizontalLayout(ingredientField, amountField, addBtn);
        shoppingForm.setAlignItems(Alignment.BASELINE);
        add(shoppingForm);


        shoppingGrid.addColumn(ShoppingItem::getIngredientName).setHeader("Ingredient");
        shoppingGrid.addColumn(ShoppingItem::getAmount).setHeader("Amount");
        shoppingGrid.addColumn(item -> item.isPurchased() ? "Yes" : "No").setHeader("Bought?");

        shoppingGrid.addComponentColumn(item -> new Button("Remove", e -> {
            shoppingRepository.delete(item);
            refreshShoppingGrid();
        })).setHeader("Remove");

        add(shoppingGrid);

        refreshGrid();
        refreshShoppingGrid();
    }

    private void refreshShoppingGrid() {
        shoppingGrid.setItems(shoppingRepository.findAll());
    }

    private void refreshGrid() {
        grid.setItems(recipeRepository.findAll());
    }
}
