package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.repository.ShoppingRepository;
import com.recipefinder.backend.service.ShoppingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "shopping", layout = MainLayout.class)
public class ShoppingListView extends VerticalLayout {

    private final ShoppingRepository shoppingRepository;
    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;
    private final ShoppingService shoppingService;
    private final Grid<ShoppingItem> shoppingGrid = new Grid<>(ShoppingItem.class, false);

    public ShoppingListView(ShoppingRepository shoppingRepository,
                            RecipeRepository recipeRepository,
                            FavoriteRepository favoriteRepository,
                            ShoppingService shoppingService) {
        this.shoppingRepository = shoppingRepository;
        this.recipeRepository = recipeRepository;
        this.favoriteRepository = favoriteRepository;
        this.shoppingService = shoppingService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "linear-gradient(180deg, #fff7ed 0%, #ffffff 38%)")
                .set("gap", "1rem");

        add(new H1("My Shopping List"));

        ComboBox<String> ingredientBox = new ComboBox<>("Ingredient");
        ingredientBox.setAllowCustomValue(false);
        ingredientBox.setPlaceholder("Start typing ingredient...");
        ingredientBox.setClearButtonVisible(true);
        ingredientBox.setWidth("320px");

        TextField amountField = new TextField("Amount");
        amountField.setPlaceholder("e.g. 3");
        amountField.setWidth("160px");

        ComboBox<String> unitBox = new ComboBox<>("Unit");
        unitBox.setAllowCustomValue(false);
        unitBox.setPlaceholder("Start typing unit...");
        unitBox.setClearButtonVisible(true);
        unitBox.setWidth("200px");

        loadSuggestions(ingredientBox, unitBox);

        Button addBtn = new Button("Add", e -> {
            if (ingredientBox.getValue() == null || ingredientBox.getValue().isBlank()) {
                Notification.show("Choose an ingredient from the list.");
                return;
            }
            if (amountField.getValue() == null || amountField.getValue().isBlank()) {
                Notification.show("Enter the amount.");
                return;
            }
            if (unitBox.getValue() == null || unitBox.getValue().isBlank()) {
                Notification.show("Choose a unit from the list.");
                return;
            }

            shoppingService.addShoppingItem(ingredientBox.getValue(), amountField.getValue().trim(), unitBox.getValue());
            ingredientBox.clear();
            amountField.clear();
            unitBox.clear();
            refreshShoppingGrid();
            loadSuggestions(ingredientBox, unitBox);
            Notification.show("Ingredient added to Shopping List!");
        });
        addBtn.setTooltipText("Add the selected ingredient to your shopping list.");

        VerticalLayout addButtonWrap = new VerticalLayout();
        addButtonWrap.setPadding(false);
        addButtonWrap.setSpacing(false);
        addButtonWrap.setAlignItems(FlexComponent.Alignment.START);
        Span addButtonLabelSpacer = new Span("Action");
        addButtonLabelSpacer.getStyle()
                .set("visibility", "hidden")
                .set("height", "var(--lumo-font-size-s)")
                .set("line-height", "var(--lumo-line-height-xs)");
        addButtonWrap.add(addButtonLabelSpacer, addBtn);

        HorizontalLayout formBar = new HorizontalLayout(ingredientBox, amountField, unitBox, addButtonWrap);
        formBar.setWidthFull();
        formBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        formBar.getStyle()
                .set("padding", "1rem")
                .set("border-radius", "22px")
                .set("background", "rgba(255,255,255,0.82)")
                .set("box-shadow", "0 18px 36px rgba(15, 23, 42, 0.08)")
                .set("flex-wrap", "wrap");

        Button recalculateButton = new Button("Recalculate & Merge", e -> {
            int mergedCount = shoppingService.mergeAndRecalculateItems();
            refreshShoppingGrid();
            loadSuggestions(ingredientBox, unitBox);
            Notification.show(mergedCount > 0
                    ? "Shopping list recalculated and merged into " + mergedCount + " items."
                    : "Shopping list is empty.");
        });
        recalculateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        recalculateButton.setTooltipText("Combine duplicate ingredients and sum compatible units such as ml with l.");

        Button deleteAllButton = new Button("Delete All", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete all shopping items?");
            dialog.setText("This will permanently remove everything from your shopping list.");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Delete all");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event -> {
                shoppingService.deleteAllShoppingItems();
                refreshShoppingGrid();
                loadSuggestions(ingredientBox, unitBox);
                Notification.show("All shopping items deleted.");
            });
            dialog.open();
        });
        deleteAllButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteAllButton.setTooltipText("Delete every item from the shopping list.");

        HorizontalLayout listActions = new HorizontalLayout(recalculateButton, deleteAllButton);
        listActions.setWidthFull();
        listActions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        listActions.getStyle().set("flex-wrap", "wrap");

        add(formBar, listActions);

        shoppingGrid.addColumn(ShoppingItem::getIngredientName).setHeader("Ingredient");
        shoppingGrid.addColumn(ShoppingItem::getAmount).setHeader("Amount");
        shoppingGrid.addColumn(ShoppingItem::getUnit).setHeader("Unit");
        shoppingGrid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox(item.isPurchased());
            checkbox.setTooltipText("Mark this ingredient as bought. Bought items move to the end of the list.");
            checkbox.addValueChangeListener(event -> {
                item.setPurchased(event.getValue());
                shoppingRepository.save(item);
                refreshShoppingGrid();
            });
            return checkbox;
        }).setHeader("Bought?");
        shoppingGrid.addComponentColumn(item -> {
            Button removeButton = new Button("Remove", e -> {
            shoppingRepository.delete(item);
            refreshShoppingGrid();
            loadSuggestions(ingredientBox, unitBox);
            });
            removeButton.setTooltipText("Remove this ingredient from the shopping list.");
            return removeButton;
        }).setHeader("Delete");
        shoppingGrid.getStyle()
                .set("border-radius", "24px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 18px 42px rgba(15, 23, 42, 0.08)")
                .set("background", "#ffffff");

        add(shoppingGrid);
        refreshShoppingGrid();
    }

    private void loadSuggestions(ComboBox<String> ingredientBox, ComboBox<String> unitBox) {
        List<String> ingredientSuggestions = shoppingService.collectIngredientSuggestions(
                recipeRepository.findAll(),
                favoriteRepository.findAll(),
                shoppingRepository.findAll()
        );
        List<String> unitSuggestions = shoppingService.collectUnitSuggestions(
                recipeRepository.findAll(),
                favoriteRepository.findAll(),
                shoppingRepository.findAll()
        );

        ingredientBox.setItems(ingredientSuggestions);
        unitBox.setItems(unitSuggestions);
    }

    private void refreshShoppingGrid() {
        List<ShoppingItem> items = shoppingRepository.findAll().stream()
                .sorted(java.util.Comparator
                        .comparing(ShoppingItem::isPurchased)
                        .thenComparing(ShoppingItem::getIngredientName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        shoppingGrid.setItems(items);
    }
}
