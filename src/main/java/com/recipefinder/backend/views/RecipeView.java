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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
public class RecipeView extends VerticalLayout {

    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;
    private final ShoppingRepository shoppingRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;

    private final Grid<Recipe> recipeGrid = new Grid<>();

    private final TextField titleFilter = new TextField();
    private final TextField categoryFilter = new TextField();
    private final TextField caloriesFilter = new TextField();

    public RecipeView(RecipeService recipeService, RecipeRepository recipeRepository,
                      ShoppingRepository shoppingRepository, FavoriteRepository favoriteRepository, AuditService auditService) {
        this.recipeService = recipeService;
        this.recipeRepository = recipeRepository;
        this.shoppingRepository = shoppingRepository;
        this.favoriteRepository = favoriteRepository;
        this.auditService = auditService;

        setSizeFull();
        add(new H1("Recipe Finder Dashboard"));

        recipeGrid.getElement().getStyle().set("--vaadin-grid-cell-white-space", "normal");

        Grid.Column<Recipe> titleColumn = recipeGrid.addColumn(Recipe::getTitle)
                .setHeader("Title").setWidth("250px").setFlexGrow(0).setSortable(true);

        Grid.Column<Recipe> categoryColumn = recipeGrid.addColumn(Recipe::getCategory)
                .setHeader("Category").setWidth("150px").setFlexGrow(0).setSortable(true);

        Grid.Column<Recipe> caloriesColumn = recipeGrid.addColumn(Recipe::getCalories)
                .setHeader("Kcal").setWidth("100px").setFlexGrow(0).setSortable(true);

        recipeGrid.addComponentColumn(recipe -> {
            Image image = new Image(recipe.getImageUrl() != null ? recipe.getImageUrl() : "", "No image");
            image.setWidth("100px"); image.setHeight("100px");
            image.getStyle().set("border-radius", "12px").set("object-fit", "cover");
            return image;
        }).setHeader("Preview").setWidth("140px").setFlexGrow(0);

        recipeGrid.addComponentColumn(this::createActions).setHeader("Actions").setWidth("230px").setFlexGrow(1);

        HeaderRow filterRow = recipeGrid.appendHeaderRow();
        setupFilter(titleFilter, "Search title...");
        filterRow.getCell(titleColumn).setComponent(titleFilter);
        setupFilter(categoryFilter, "Search category...");
        filterRow.getCell(categoryColumn).setComponent(categoryFilter);
        setupFilter(caloriesFilter, "Max kcal...");
        filterRow.getCell(caloriesColumn).setComponent(caloriesFilter);

        Button generateBtn = new Button("Generate Daily Menu", e -> {
            recipeService.generateDailyMenu();
            refreshGrid();
        });

        Button fetchBtn = new Button("Fetch Random Recipe", e -> {
            recipeService.fetchAndSaveRandomRecipe();
            refreshGrid();
        });

        Button deleteAllBtn = new Button("Delete All", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete all recipes?");
            dialog.setText("Are you sure you want to permanently remove all recipes from your dashboard?");
            dialog.setCancelable(true);
            dialog.setCancelText("No, keep them");
            dialog.setConfirmText("Yes, delete all");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(event -> {
                recipeService.deleteAllRecipes();
                // Czyścimy filtry, żeby pokazać pustą bazę
                titleFilter.clear();
                categoryFilter.clear();
                caloriesFilter.clear();
                refreshGrid();
                Notification.show("All recipes deleted successfully!");
            });
            dialog.open();
        });
        deleteAllBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        add(new HorizontalLayout(generateBtn, fetchBtn, deleteAllBtn), recipeGrid);
        refreshGrid();
    }

    private void setupFilter(TextField filter, String placeholder) {
        filter.setPlaceholder(placeholder);
        filter.setClearButtonVisible(true);
        filter.setWidthFull();
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> refreshGrid());
    }

    private HorizontalLayout createActions(Recipe recipe) {
        Button favBtn = new Button("❤️", e -> {
            if (!favoriteRepository.existsByRecipeId(recipe.getId())) {
                favoriteRepository.save(Favorite.builder().recipe(recipe).build());
                auditService.log("UI_ADD_FAVORITE", "Added: " + recipe.getTitle());
                Notification.show("Added to Favorites!");
            } else { Notification.show("Already in Favorites!"); }
        });
        favBtn.setTooltipText("Add to Favorites");

        Button cartBtn = new Button("🛒", e -> {
            shoppingRepository.save(ShoppingItem.builder()
                    .ingredientName("Ingredients for: " + recipe.getTitle())
                    .amount("1 set").isPurchased(false).build());
            auditService.log("UI_ADD_TO_CART", "Recipe: " + recipe.getTitle());
            Notification.show("Added to Shopping List!");
        });
        cartBtn.setTooltipText("Add to Shopping List");

        Button deleteBtn = new Button("🗑️", e -> {
            recipeRepository.delete(recipe);
            auditService.log("UI_DELETE_RECIPE", "Deleted: " + recipe.getTitle());
            refreshGrid();
            Notification.show("Recipe removed");
        });
        deleteBtn.setTooltipText("Remove from dashboard");

        return new HorizontalLayout(favBtn, cartBtn, deleteBtn);
    }

    private void refreshGrid() {
        // Pobieramy nową listę z bazy i rzutujemy na ArrayList, żeby mieć pewność świeżości
        List<Recipe> allRecipes = new ArrayList<>();
        recipeRepository.findAll().forEach(allRecipes::add);

        List<Recipe> filteredRecipes = allRecipes.stream()
                .filter(r -> matches(r.getTitle(), titleFilter.getValue()))
                .filter(r -> matches(r.getCategory(), categoryFilter.getValue()))
                .filter(r -> matchesCalories(r.getCalories(), caloriesFilter.getValue()))
                .collect(Collectors.toList());

        recipeGrid.setItems(filteredRecipes);
    }

    private boolean matches(String value, String filter) {
        return filter == null || filter.isEmpty() ||
                (value != null && value.toLowerCase().contains(filter.toLowerCase()));
    }

    private boolean matchesCalories(Integer value, String filter) {
        if (filter == null || filter.isEmpty()) return true;
        try {
            int maxKcal = Integer.parseInt(filter);
            return value != null && value <= maxKcal;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
