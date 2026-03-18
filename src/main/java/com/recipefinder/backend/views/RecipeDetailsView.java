package com.recipefinder.backend.views;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.repository.FavoriteRepository;
import com.recipefinder.backend.repository.RecipeRepository;
import com.recipefinder.backend.service.AuditService;
import com.recipefinder.backend.service.ShoppingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.Optional;

@Route(value = "recipe-details", layout = MainLayout.class)
public class RecipeDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuditService auditService;
    private final ShoppingService shoppingService;

    public RecipeDetailsView(RecipeRepository recipeRepository,
                             FavoriteRepository favoriteRepository,
                             AuditService auditService,
                             ShoppingService shoppingService) {
        this.recipeRepository = recipeRepository;
        this.favoriteRepository = favoriteRepository;
        this.auditService = auditService;
        this.shoppingService = shoppingService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "linear-gradient(180deg, #fff7ed 0%, #ffffff 42%)");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String type = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("type", java.util.List.of("recipe"))
                .get(0);
        String idValue = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("id", java.util.List.of())
                .stream()
                .findFirst()
                .orElse(null);

        renderView(type, idValue);
    }

    private void renderView(String type, String idValue) {
        removeAll();

        if (idValue == null) {
            add(createEmptyState("Recipe details are unavailable.", "Choose a recipe from the dashboard or favorites."));
            return;
        }

        try {
            long id = Long.parseLong(idValue);
            if ("favorite".equalsIgnoreCase(type)) {
                favoriteRepository.findById(id)
                        .ifPresentOrElse(favorite -> add(buildDetailsLayout(toRecipeCardData(favorite), ViewContext.favorite(id, favorite))),
                                () -> add(createEmptyState("Favorite not found.", "This favorite may have already been removed.")));
            } else {
                recipeRepository.findById(id)
                        .ifPresentOrElse(recipe -> add(buildDetailsLayout(toRecipeCardData(recipe), ViewContext.recipe(id, recipe))),
                                () -> add(createEmptyState("Recipe not found.", "This recipe is no longer available on the dashboard.")));
            }
        } catch (NumberFormatException exception) {
            add(createEmptyState("Invalid recipe identifier.", "Open the details page again from the recipe list."));
        }
    }

    private Component buildDetailsLayout(RecipeCardData data, ViewContext context) {
        VerticalLayout page = new VerticalLayout();
        page.setWidthFull();
        page.setMaxWidth("1040px");
        page.setPadding(false);
        page.setSpacing(true);
        page.getStyle().set("margin", "0 auto");

        Button backButton = new Button("Back", event -> getUI().ifPresent(ui -> ui.navigate(context.backTarget())));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle()
                .set("border-radius", "999px")
                .set("font-weight", "700");
        backButton.setTooltipText("Go back to the previous recipe list.");

        HorizontalLayout hero = new HorizontalLayout();
        hero.setWidthFull();
        hero.setSpacing(true);
        hero.setAlignItems(FlexComponent.Alignment.STRETCH);
        hero.getStyle()
                .set("padding", "1.75rem")
                .set("border-radius", "30px")
                .set("background", "linear-gradient(135deg, #fff7ed 0%, #ffedd5 40%, #ffffff 100%)")
                .set("box-shadow", "0 24px 52px rgba(120, 53, 15, 0.15)")
                .set("gap", "1.5rem")
                .set("flex-wrap", "wrap");

        Image heroImage = new Image(data.imageUrl(), data.title());
        heroImage.setWidth("360px");
        heroImage.setHeight("360px");
        heroImage.getStyle()
                .set("width", "min(100%, 360px)")
                .set("border-radius", "24px")
                .set("object-fit", "cover")
                .set("box-shadow", "0 18px 38px rgba(15, 23, 42, 0.18)");

        VerticalLayout copy = new VerticalLayout();
        copy.setPadding(false);
        copy.setSpacing(true);
        copy.setWidthFull();
        copy.getStyle().set("min-width", "280px");

        Span eyebrow = new Span("Recipe Details");
        eyebrow.getStyle()
                .set("font-size", "0.85rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.08em")
                .set("text-transform", "uppercase")
                .set("color", "#c2410c");

        H1 title = new H1(data.title());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "clamp(2rem, 4vw, 3.2rem)")
                .set("line-height", "1.05")
                .set("color", "#431407");

        Paragraph intro = new Paragraph("A cleaner, more focused view of the dish with the most useful details in one place.");
        intro.getStyle()
                .set("margin", "0")
                .set("font-size", "1rem")
                .set("line-height", "1.7")
                .set("color", "#7c2d12");

        HorizontalLayout chips = new HorizontalLayout(
                createChip("Category", data.category()),
                createChip("Calories", data.calories() != null ? data.calories() + " kcal" : "Unknown")
        );
        chips.setSpacing(true);
        chips.getStyle().set("flex-wrap", "wrap");

        copy.add(eyebrow, title, intro, chips, createFavoritesButton(context));
        hero.add(heroImage, copy);

        HorizontalLayout sections = new HorizontalLayout();
        sections.setWidthFull();
        sections.setSpacing(true);
        sections.getStyle()
                .set("gap", "1.5rem")
                .set("flex-wrap", "wrap");

        VerticalLayout instructionsCard = createCard(
                "Instructions",
                data.instructions() != null && !data.instructions().isBlank()
                        ? data.instructions()
                        : "No instructions were provided for this recipe."
        );
        instructionsCard.setWidthFull();
        instructionsCard.getStyle().set("flex", "2 1 560px");

        VerticalLayout ingredientsCard = createCard(
                "Ingredients",
                data.ingredients() != null && !data.ingredients().isBlank()
                        ? data.ingredients()
                        : "No ingredients were provided for this recipe."
        );
        ingredientsCard.setWidthFull();
        ingredientsCard.getStyle().set("flex", "1 1 280px");
        Button shoppingButton = createShoppingButton(context);
        ingredientsCard.addComponentAsFirst(shoppingButton);

        sections.add(instructionsCard, ingredientsCard);
        page.add(backButton, hero, sections);
        return page;
    }

    private Button createFavoritesButton(ViewContext context) {
        Optional<Favorite> linkedFavorite = context.recipe() != null
                ? favoriteRepository.findByRecipeId(context.recipe().getId())
                : Optional.empty();

        if (linkedFavorite.isPresent()) {
            Button removeButton = new Button("Remove from Favorites", event -> {
                favoriteRepository.delete(linkedFavorite.get());
                auditService.log("UI_REMOVE_FAVORITE", "Removed: " + linkedFavorite.get().getRecipeTitle());
                Notification.show("Removed from favorites");
                if ("favorite".equals(context.type())) {
                    getUI().ifPresent(ui -> ui.navigate(FavoritesView.class));
                } else {
                    renderView(context.type(), String.valueOf(context.id()));
                }
            });
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            stylePrimaryAction(removeButton, true);
            removeButton.setTooltipText("Remove this recipe from favorites.");
            return removeButton;
        }

        if ("favorite".equals(context.type())) {
            Button infoButton = new Button("Saved in Favorites");
            infoButton.setEnabled(false);
            stylePrimaryAction(infoButton, false);
            infoButton.setTooltipText("This recipe is already stored in favorites.");
            return infoButton;
        }

        Button addButton = new Button("Add to Favorites", event -> {
            Recipe recipe = context.recipe();
            if (recipe == null) {
                Notification.show("This recipe cannot be added to favorites right now.");
                return;
            }

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
            Notification.show("Added to favorites");
            renderView(context.type(), String.valueOf(context.id()));
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        stylePrimaryAction(addButton, false);
        addButton.setTooltipText("Save this recipe to favorites.");
        return addButton;
    }

    private void stylePrimaryAction(Button button, boolean destructive) {
        button.getStyle()
                .set("border-radius", "999px")
                .set("padding", "0.85rem 1.25rem")
                .set("font-weight", "700")
                .set("box-shadow", destructive
                        ? "0 12px 30px rgba(220, 38, 38, 0.18)"
                        : "0 14px 32px rgba(234, 88, 12, 0.18)");
    }

    private Button createShoppingButton(ViewContext context) {
        Button shoppingButton = new Button("Add Ingredients to Shopping List", event -> {
            int addedItems;
            if ("favorite".equals(context.type())) {
                Favorite favorite = favoriteRepository.findById(context.id()).orElse(null);
                addedItems = favorite != null ? shoppingService.addFavoriteIngredients(favorite) : 0;
            } else {
                Recipe recipe = context.recipe();
                addedItems = recipe != null ? shoppingService.addRecipeIngredients(recipe) : 0;
            }

            Notification.show(addedItems > 0
                    ? "Added " + addedItems + " ingredients to Shopping List!"
                    : "No ingredients found for this recipe.");
        });
        shoppingButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        stylePrimaryAction(shoppingButton, false);
        shoppingButton.setTooltipText("Add all ingredients from this recipe to the shopping list.");
        return shoppingButton;
    }

    private VerticalLayout createCard(String title, String body) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("border-radius", "24px")
                .set("background", "#ffffff")
                .set("box-shadow", "0 18px 42px rgba(15, 23, 42, 0.08)");

        H2 heading = new H2(title);
        heading.getStyle()
                .set("margin", "0")
                .set("font-size", "1.35rem")
                .set("color", "#1f2937");

        Paragraph content = new Paragraph(body);
        content.getStyle()
                .set("margin", "0")
                .set("white-space", "pre-line")
                .set("line-height", "1.8")
                .set("color", "#334155");

        card.add(heading, content);
        return card;
    }

    private Span createChip(String label, String value) {
        Span chip = new Span(label + ": " + value);
        chip.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("padding", "0.65rem 1rem")
                .set("border-radius", "999px")
                .set("background", "rgba(255,255,255,0.82)")
                .set("font-weight", "700")
                .set("color", "#9a3412")
                .set("box-shadow", "inset 0 0 0 1px rgba(251, 146, 60, 0.3)");
        return chip;
    }

    private Component createEmptyState(String title, String message) {
        Div box = new Div();
        box.getStyle()
                .set("max-width", "720px")
                .set("margin", "2rem auto")
                .set("padding", "2rem")
                .set("border-radius", "24px")
                .set("background", "#fff7ed")
                .set("box-shadow", "0 18px 42px rgba(120, 53, 15, 0.10)");

        H2 heading = new H2(title);
        heading.getStyle().set("margin", "0 0 0.75rem 0");

        Paragraph body = new Paragraph(message);
        body.getStyle().set("margin", "0");

        box.add(heading, body);
        return box;
    }

    private RecipeCardData toRecipeCardData(Recipe recipe) {
        return new RecipeCardData(
                recipe.getTitle(),
                recipe.getCategory(),
                recipe.getCalories(),
                recipe.getImageUrl() != null ? recipe.getImageUrl() : "",
                recipe.getInstructions(),
                recipe.getIngredients()
        );
    }

    private RecipeCardData toRecipeCardData(Favorite favorite) {
        if (favorite.getRecipe() != null) {
            return toRecipeCardData(favorite.getRecipe());
        }

        return new RecipeCardData(
                favorite.getRecipeTitle(),
                favorite.getRecipeCategory(),
                favorite.getRecipeCalories(),
                favorite.getRecipeImageUrl() != null ? favorite.getRecipeImageUrl() : "",
                favorite.getRecipeInstructions(),
                favorite.getRecipeIngredients()
        );
    }

    private record RecipeCardData(
            String title,
            String category,
            Integer calories,
            String imageUrl,
            String instructions,
            String ingredients
    ) {
    }

    private record ViewContext(
            String type,
            long id,
            Class<? extends Component> backTarget,
            Recipe recipe
    ) {
        private static ViewContext recipe(long id, Recipe recipe) {
            return new ViewContext("recipe", id, RecipeView.class, recipe);
        }

        private static ViewContext favorite(long id, Favorite favorite) {
            return new ViewContext("favorite", id, FavoritesView.class, favorite.getRecipe());
        }
    }
}
