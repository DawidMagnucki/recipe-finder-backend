package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.service.RecipeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;
    private final Grid<Recipe> grid = new Grid<>(Recipe.class);

    public MainView(RecipeService recipeService, RecipeRepository recipeRepository) {
        this.recipeService = recipeService;
        this.recipeRepository = recipeRepository;

        setAlignItems(Alignment.CENTER);
        add(new H1("Recipe Finder Dashboard"));

        grid.setColumns("title", "category");

        grid.addComponentColumn(recipe -> {
            Image image = new Image(recipe.getImageUrl(), "No image");
            image.setWidth("80px");
            image.setHeight("80px");
            return image;
        }).setHeader("Preview");

        Button generateBtn = new Button("Generate Daily Menu Now", e -> {
            recipeService.generateDailyMenu();
            refreshGrid();
            Notification.show("Daily menu generated and saved to MySQL!");
        });

        Button randomBtn = new Button("Fetch Random Recipe", e -> {
            recipeService.fetchAndSaveRandomRecipe();
            refreshGrid();
            Notification.show("Random recipe added!");
        });

        add(generateBtn, randomBtn, grid);
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(recipeRepository.findAll());
    }
}