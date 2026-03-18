package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.ShoppingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

@Route(value = "favorites", layout = MainLayout.class)
public class FavoritesView extends VerticalLayout {

    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;
    private final ShoppingService shoppingService;
    private final Grid<Favorite> favoriteGrid = new Grid<>(Favorite.class, false);

    private final TextField titleFilter = new TextField();
    private final TextField categoryFilter = new TextField();
    private final TextField caloriesFilter = new TextField();

    public FavoritesView(FavoriteRepository favoriteRepository, AuditService auditService, ShoppingService shoppingService) {
        this.favoriteRepository = favoriteRepository;
        this.auditService = auditService;
        this.shoppingService = shoppingService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "linear-gradient(180deg, #fff7ed 0%, #ffffff 38%)")
                .set("gap", "1rem");

        add(new H1("My Favorites"));
        favoriteGrid.getStyle()
                .set("border-radius", "24px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 18px 42px rgba(15, 23, 42, 0.08)")
                .set("background", "#ffffff");

        Grid.Column<Favorite> titleColumn = favoriteGrid.addComponentColumn(this::createFavoriteTitleLink)
                .setHeader("Recipe Title")
                .setAutoWidth(true);
        favoriteGrid.addComponentColumn(this::createFavoritePreviewLink)
                .setHeader("Preview")
                .setAutoWidth(true);
        Grid.Column<Favorite> categoryColumn = favoriteGrid.addColumn(this::getCategory)
                .setHeader("Category")
                .setAutoWidth(true);
        Grid.Column<Favorite> caloriesColumn = favoriteGrid.addColumn(this::getCalories)
                .setHeader("Kcal")
                .setAutoWidth(true);
        favoriteGrid.addComponentColumn(this::createActions).setHeader("Actions");

        HeaderRow filterRow = favoriteGrid.appendHeaderRow();
        setupFilter(titleFilter, "Search title...");
        filterRow.getCell(titleColumn).setComponent(titleFilter);
        setupFilter(categoryFilter, "Search category...");
        filterRow.getCell(categoryColumn).setComponent(categoryFilter);
        setupFilter(caloriesFilter, "Max kcal...");
        filterRow.getCell(caloriesColumn).setComponent(caloriesFilter);

        add(favoriteGrid);
        refreshFavoriteGrid();
    }

    private RouterLink createFavoriteTitleLink(Favorite favorite) {
        RouterLink link = new RouterLink();
        link.setRoute(RecipeDetailsView.class);
        link.setQueryParameters(QueryParameters.simple(java.util.Map.of(
                "type", "favorite",
                "id", String.valueOf(favorite.getId())
        )));

        Span title = new Span(getTitle(favorite));
        title.getStyle()
                .set("font-weight", "700")
                .set("color", "var(--lumo-primary-text-color)")
                .set("line-height", "1.4");
        link.add(title);
        link.getStyle().set("text-decoration", "none");
        return link;
    }

    private RouterLink createFavoritePreviewLink(Favorite favorite) {
        RouterLink link = new RouterLink();
        link.setRoute(RecipeDetailsView.class);
        link.setQueryParameters(QueryParameters.simple(java.util.Map.of(
                "type", "favorite",
                "id", String.valueOf(favorite.getId())
        )));

        Image image = new Image(getImageUrl(favorite), getTitle(favorite));
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

    private HorizontalLayout createActions(Favorite favorite) {
        Button shoppingButton = new Button(VaadinIcon.CART.create(), event -> {
            int addedItems = shoppingService.addFavoriteIngredients(favorite);
            Notification.show(addedItems > 0
                    ? "Added " + addedItems + " ingredients to Shopping List!"
                    : "No ingredients found for this favorite.");
        });
        shoppingButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        shoppingButton.setTooltipText("Add this favorite recipe's ingredients to the shopping list.");

        Button removeButton = new Button(VaadinIcon.TRASH.create(), event -> {
            String title = getTitle(favorite);
            favoriteRepository.delete(favorite);
            auditService.log("UI_REMOVE_FAVORITE", "Removed: " + title);
            refreshFavoriteGrid();
        });
        removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        removeButton.setTooltipText("Remove this recipe from favorites.");

        HorizontalLayout actions = new HorizontalLayout(shoppingButton, removeButton);
        actions.getStyle().set("flex-wrap", "wrap");
        return actions;
    }

    private void setupFilter(TextField filter, String placeholder) {
        filter.setPlaceholder(placeholder);
        filter.setClearButtonVisible(true);
        filter.setWidthFull();
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(event -> refreshFavoriteGrid());
    }

    private void refreshFavoriteGrid() {
        List<Favorite> allFavorites = new ArrayList<>(favoriteRepository.findAll());

        List<Favorite> filteredFavorites = allFavorites.stream()
                .filter(favorite -> matches(getTitle(favorite), titleFilter.getValue()))
                .filter(favorite -> matches(getCategory(favorite), categoryFilter.getValue()))
                .filter(favorite -> matchesCalories(getCalories(favorite), caloriesFilter.getValue()))
                .collect(Collectors.toList());

        favoriteGrid.setItems(filteredFavorites);
    }

    private String getTitle(Favorite favorite) {
        return favorite.getRecipe() != null ? favorite.getRecipe().getTitle() : favorite.getRecipeTitle();
    }

    private String getCategory(Favorite favorite) {
        return favorite.getRecipe() != null ? favorite.getRecipe().getCategory() : favorite.getRecipeCategory();
    }

    private Integer getCalories(Favorite favorite) {
        return favorite.getRecipe() != null ? favorite.getRecipe().getCalories() : favorite.getRecipeCalories();
    }

    private String getImageUrl(Favorite favorite) {
        String imageUrl = favorite.getRecipe() != null ? favorite.getRecipe().getImageUrl() : favorite.getRecipeImageUrl();
        return imageUrl != null ? imageUrl : "";
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
        } catch (NumberFormatException exception) {
            return true;
        }
    }

}
