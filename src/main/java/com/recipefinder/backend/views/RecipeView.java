package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.RecipeService;
import com.recipefinder.backend.service.ShoppingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
public class RecipeView extends VerticalLayout {

    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;
    private final ShoppingService shoppingService;

    private final Grid<Recipe> recipeGrid = new Grid<>();

    private final TextField titleFilter = new TextField();
    private final TextField categoryFilter = new TextField();
    private final TextField caloriesFilter = new TextField();

    public RecipeView(RecipeService recipeService,
                      RecipeRepository recipeRepository,
                      FavoriteRepository favoriteRepository,
                      AuditService auditService,
                      ShoppingService shoppingService) {
        this.recipeService = recipeService;
        this.recipeRepository = recipeRepository;
        this.favoriteRepository = favoriteRepository;
        this.auditService = auditService;
        this.shoppingService = shoppingService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "linear-gradient(180deg, #fff7ed 0%, #ffffff 38%)")
                .set("gap", "1rem");

        add(new H1("Recipe Finder Dashboard"));

        recipeGrid.getElement().getStyle().set("--vaadin-grid-cell-white-space", "normal");
        recipeGrid.getStyle()
                .set("border-radius", "24px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 18px 42px rgba(15, 23, 42, 0.08)")
                .set("background", "#ffffff");

        Grid.Column<Recipe> titleColumn = recipeGrid.addComponentColumn(this::createRecipeTitleLink)
                .setHeader("Title").setWidth("250px").setFlexGrow(0).setSortable(true);

        Grid.Column<Recipe> categoryColumn = recipeGrid.addColumn(Recipe::getCategory)
                .setHeader("Category").setWidth("150px").setFlexGrow(0).setSortable(true);

        Grid.Column<Recipe> caloriesColumn = recipeGrid.addColumn(Recipe::getCalories)
                .setHeader("Kcal").setWidth("150px").setFlexGrow(0).setSortable(true);

        recipeGrid.addComponentColumn(this::createRecipePreviewLink)
                .setHeader("Preview").setWidth("140px").setFlexGrow(0);

        recipeGrid.addComponentColumn(this::createActions)
                .setHeader("Actions")
                .setAutoWidth(true);

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
        generateBtn.setTooltipText("Generate three meals for the day and add them to the dashboard.");

        Button fetchBtn = new Button("Fetch Random Recipe", e -> {
            recipeService.fetchAndSaveRandomRecipe();
            refreshGrid();
        });
        fetchBtn.setTooltipText("Fetch one random recipe and add it to the dashboard.");

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
                titleFilter.clear();
                categoryFilter.clear();
                caloriesFilter.clear();
                refreshGrid();
                Notification.show("All recipes deleted successfully!");
            });
            dialog.open();
        });
        deleteAllBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteAllBtn.setTooltipText("Delete all recipes from the dashboard.");

        HorizontalLayout actionsBar = new HorizontalLayout(generateBtn, fetchBtn, deleteAllBtn);
        actionsBar.setWidthFull();
        actionsBar.getStyle()
                .set("padding", "1rem")
                .set("border-radius", "22px")
                .set("background", "rgba(255,255,255,0.82)")
                .set("box-shadow", "0 18px 36px rgba(15, 23, 42, 0.08)")
                .set("backdrop-filter", "blur(10px)")
                .set("flex-wrap", "wrap");

        add(actionsBar, recipeGrid);
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
        Button favoriteButton = new Button(VaadinIcon.HEART.create(), e -> {
            if (!favoriteRepository.existsByRecipeId(recipe.getId())) {
                favoriteRepository.save(Favorite.builder()
                        .recipe(recipe)
                        .recipeIdSnapshot(recipe.getId())
                        .recipeTitle(recipe.getTitle())
                        .recipeCategory(recipe.getCategory())
                        .recipeCalories(recipe.getCalories())
                        .recipeImageUrl(recipe.getImageUrl())
                        .recipeInstructions(recipe.getInstructions())
                        .recipeIngredients(recipe.getIngredients())
                        .build());
                auditService.log("UI_ADD_FAVORITE", "Added: " + recipe.getTitle());
                Notification.show("Added to Favorites!");
            } else {
                Notification.show("Already in Favorites!");
            }
        });
        favoriteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        favoriteButton.setTooltipText("Add this recipe to favorites.");

        Button shoppingButton = new Button(VaadinIcon.CART.create(), e -> {
            int addedItems = shoppingService.addRecipeIngredients(recipe);
            Notification.show(addedItems > 0
                    ? "Added " + addedItems + " ingredients to Shopping List!"
                    : "No ingredients found for this recipe.");
        });
        shoppingButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        shoppingButton.setTooltipText("Add this recipe's ingredients to the shopping list.");

        Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> {
            recipeService.deleteRecipe(recipe);
            auditService.log("UI_DELETE_RECIPE", "Deleted: " + recipe.getTitle());
            refreshGrid();
            Notification.show("Recipe removed");
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        deleteButton.setTooltipText("Remove this recipe from the dashboard.");

        HorizontalLayout actions = new HorizontalLayout(favoriteButton, shoppingButton, deleteButton);
        actions.getStyle().set("flex-wrap", "wrap");
        return actions;
    }

    private RouterLink createRecipeTitleLink(Recipe recipe) {
        RouterLink link = new RouterLink();
        link.setRoute(RecipeDetailsView.class);
        link.setQueryParameters(QueryParameters.simple(java.util.Map.of(
                "type", "recipe",
                "id", String.valueOf(recipe.getId())
        )));

        Span title = new Span(recipe.getTitle());
        title.getStyle()
                .set("font-weight", "700")
                .set("color", "var(--lumo-primary-text-color)")
                .set("line-height", "1.4");
        link.add(title);
        link.getStyle().set("text-decoration", "none");
        return link;
    }

    private RouterLink createRecipePreviewLink(Recipe recipe) {
        RouterLink link = new RouterLink();
        link.setRoute(RecipeDetailsView.class);
        link.setQueryParameters(QueryParameters.simple(java.util.Map.of(
                "type", "recipe",
                "id", String.valueOf(recipe.getId())
        )));

        Image image = new Image(recipe.getImageUrl() != null ? recipe.getImageUrl() : "", recipe.getTitle());
        image.setWidth("100px");
        image.setHeight("100px");
        image.getStyle()
                .set("border-radius", "16px")
                .set("object-fit", "cover")
                .set("box-shadow", "0 10px 24px rgba(15, 23, 42, 0.16)");
        link.add(image);
        link.getStyle().set("display", "inline-flex");
        return link;
    }

    private void refreshGrid() {
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
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        try {
            int maxKcal = Integer.parseInt(filter);
            return value != null && value <= maxKcal;
        } catch (NumberFormatException e) {
            return true;
        }
    }

}
