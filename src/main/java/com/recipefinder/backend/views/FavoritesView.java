package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.service.AuditService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "favorites", layout = MainLayout.class)
public class FavoritesView extends VerticalLayout {

    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;
    private final Grid<Favorite> favoriteGrid = new Grid<>(Favorite.class);

    public FavoritesView(FavoriteRepository favoriteRepository, AuditService auditService) {
        this.favoriteRepository = favoriteRepository;
        this.auditService = auditService;

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
        refreshFavoriteGrid();
    }

    private void refreshFavoriteGrid() { favoriteGrid.setItems(favoriteRepository.findAll()); }
}
