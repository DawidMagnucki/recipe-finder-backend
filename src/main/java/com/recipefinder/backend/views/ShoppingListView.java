package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.ShoppingRepository;
import com.recipefinder.backend.service.AuditService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route(value = "shopping", layout = MainLayout.class)
public class ShoppingListView extends VerticalLayout {

    private final ShoppingRepository shoppingRepository;
    private final AuditService auditService;
    private final Grid<ShoppingItem> shoppingGrid = new Grid<>(ShoppingItem.class);

    public ShoppingListView(ShoppingRepository shoppingRepository, AuditService auditService) {
        this.shoppingRepository = shoppingRepository;
        this.auditService = auditService;

        add(new H1("My Shopping List 🛒"));

        TextField ingredientField = new TextField("Ingredient");
        TextField amountField = new TextField("Amount");
        Button addBtn = new Button("Add", e -> {
            if (!ingredientField.isEmpty()) {
                shoppingRepository.save(ShoppingItem.builder()
                        .ingredientName(ingredientField.getValue())
                        .amount(amountField.getValue())
                        .isPurchased(false).build());
                auditService.log("UI_ADD_ITEM", ingredientField.getValue());
                ingredientField.clear(); amountField.clear();
                refreshShoppingGrid();
            }
        });
        add(new HorizontalLayout(ingredientField, amountField, addBtn));

        shoppingGrid.setColumns("ingredientName", "amount");
        shoppingGrid.addColumn(item -> item.isPurchased() ? "Yes" : "No").setHeader("Bought?");
        shoppingGrid.addComponentColumn(item -> new Button("❌", e -> {
            String name = item.getIngredientName();
            shoppingRepository.delete(item);
            auditService.log("UI_DELETE_ITEM", name);
            refreshShoppingGrid();
        })).setHeader("Delete");

        add(shoppingGrid);
        refreshShoppingGrid();
    }

    private void refreshShoppingGrid() { shoppingGrid.setItems(shoppingRepository.findAll()); }
}
