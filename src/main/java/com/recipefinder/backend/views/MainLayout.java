package com.recipefinder.backend.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Recipe Finder");
        logo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle()
                .set("padding", "1rem 1.25rem")
                .set("background", "linear-gradient(90deg, #fff7ed 0%, #ffffff 100%)")
                .set("box-shadow", "0 10px 28px rgba(15, 23, 42, 0.06)");

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout(
                new RouterLink("Dashboard", RecipeView.class),
                new RouterLink("My Favorites", FavoritesView.class),
                new RouterLink("Shopping List", ShoppingListView.class)
        );
        drawer.getStyle()
                .set("padding", "1rem")
                .set("gap", "0.75rem")
                .set("background", "linear-gradient(180deg, #fff7ed 0%, #ffffff 55%)");
        addToDrawer(drawer);
    }
}
