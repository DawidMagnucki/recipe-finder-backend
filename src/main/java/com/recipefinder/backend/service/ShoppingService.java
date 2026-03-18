package com.recipefinder.backend.service;

import com.recipefinder.backend.domain.Favorite;
import com.recipefinder.backend.domain.Recipe;
import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.repository.ShoppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ShoppingService {

    private static final Pattern QUANTITY_AND_UNIT_PATTERN = Pattern.compile("^(\\d+(?:[.,]\\d+)?|\\d+/\\d+)([a-zA-Z]+)$");

    private static final Set<String> UNIT_WORDS = Set.of(
            "g", "kg", "mg", "ml", "l", "lb", "lbs", "oz",
            "cup", "cups", "tbsp", "tsp", "teaspoon", "teaspoons",
            "tablespoon", "tablespoons", "pinch", "pinches", "dash",
            "clove", "cloves", "slice", "slices", "can", "cans",
            "packet", "packets", "package", "packages", "bunch", "bunches",
            "sprig", "sprigs", "piece", "pieces", "tbs", "tbl", "tsps"
    );

    private static final Map<String, String> UNIT_ALIASES = Map.ofEntries(
            Map.entry("teaspoon", "tsp"),
            Map.entry("teaspoons", "tsp"),
            Map.entry("tablespoon", "tbsp"),
            Map.entry("tablespoons", "tbsp"),
            Map.entry("tbs", "tbsp"),
            Map.entry("tbl", "tbsp"),
            Map.entry("tsps", "tsp"),
            Map.entry("cups", "cup"),
            Map.entry("pinches", "pinch"),
            Map.entry("cloves", "clove"),
            Map.entry("slices", "slice"),
            Map.entry("cans", "can"),
            Map.entry("packets", "packet"),
            Map.entry("packages", "package"),
            Map.entry("bunches", "bunch"),
            Map.entry("sprigs", "sprig"),
            Map.entry("pieces", "piece"),
            Map.entry("lbs", "lb")
    );

    private final ShoppingRepository shoppingRepository;
    private final AuditService auditService;

    public void addShoppingItem(String ingredientName, String amount, String unit) {
        shoppingRepository.save(ShoppingItem.builder()
                .ingredientName(capitalizeWords(ingredientName))
                .amount(amount.trim())
                .unit(normalizeDisplayUnit(unit))
                .isPurchased(false)
                .build());
        auditService.log("UI_ADD_ITEM", ingredientName + " (" + amount + " " + unit + ")");
    }

    public int addRecipeIngredients(Recipe recipe) {
        List<IngredientEntry> entries = parseIngredients(recipe.getIngredients());
        entries.forEach(entry -> addShoppingItem(entry.ingredientName(), entry.amount(), entry.unit()));
        auditService.log("UI_ADD_RECIPE_TO_CART", "Recipe: " + recipe.getTitle() + ", items: " + entries.size());
        return entries.size();
    }

    public int addFavoriteIngredients(Favorite favorite) {
        String title = favorite.getRecipe() != null ? favorite.getRecipe().getTitle() : favorite.getRecipeTitle();
        String ingredients = favorite.getRecipe() != null ? favorite.getRecipe().getIngredients() : favorite.getRecipeIngredients();
        List<IngredientEntry> entries = parseIngredients(ingredients);
        entries.forEach(entry -> addShoppingItem(entry.ingredientName(), entry.amount(), entry.unit()));
        auditService.log("UI_ADD_FAVORITE_TO_CART", "Favorite: " + title + ", items: " + entries.size());
        return entries.size();
    }

    public void deleteAllShoppingItems() {
        shoppingRepository.deleteAll();
        auditService.log("UI_DELETE_ALL_SHOPPING_ITEMS", "All shopping items removed");
    }

    public int mergeAndRecalculateItems() {
        List<ShoppingItem> items = shoppingRepository.findAll();
        if (items.isEmpty()) {
            return 0;
        }

        Map<String, AggregateBucket> merged = new LinkedHashMap<>();
        for (ShoppingItem item : items) {
            String ingredientName = capitalizeWords(item.getIngredientName() != null ? item.getIngredientName().trim() : "");
            if (ingredientName.isBlank()) {
                continue;
            }

            String unit = normalizeDisplayUnit(item.getUnit());
            BigDecimal amountValue = parseAmountValue(item.getAmount());
            String groupUnit = toAggregationGroupUnit(unit);
            BigDecimal normalizedAmount = convertToGroupUnit(amountValue, unit, groupUnit);
            String key = ingredientName.toLowerCase(Locale.ROOT) + "|" + groupUnit;

            merged.computeIfAbsent(key, ignored -> new AggregateBucket(ingredientName, groupUnit))
                    .add(normalizedAmount, item.isPurchased());
        }

        shoppingRepository.deleteAll();
        merged.values().forEach(bucket -> shoppingRepository.save(ShoppingItem.builder()
                .ingredientName(bucket.ingredientName())
                .amount(bucket.finalAmount())
                .unit(bucket.finalUnit())
                .isPurchased(bucket.purchased())
                .build()));

        auditService.log("UI_MERGE_SHOPPING_ITEMS", "Merged shopping items from " + items.size() + " to " + merged.size());
        return merged.size();
    }

    public List<IngredientEntry> parseIngredients(String ingredientsText) {
        if (ingredientsText == null || ingredientsText.isBlank()) {
            return List.of();
        }

        List<IngredientEntry> entries = new ArrayList<>();
        for (String rawLine : ingredientsText.split("\\R")) {
            String line = rawLine.trim();
            if (!line.isEmpty()) {
                entries.add(parseIngredientLine(line));
            }
        }
        return entries;
    }

    private IngredientEntry parseIngredientLine(String line) {
        List<String> tokens = new ArrayList<>(Arrays.asList(line.trim().split("\\s+")));
        if (!tokens.isEmpty()) {
            splitCombinedQuantityAndUnit(tokens);
        }

        int index = 0;
        List<String> amountTokens = new ArrayList<>();
        while (index < tokens.size() && isQuantityToken(tokens.get(index))) {
            amountTokens.add(tokens.get(index));
            index++;
        }

        String unit = "item";
        if (index < tokens.size() && isUnitToken(tokens.get(index))) {
            unit = normalizeDisplayUnit(tokens.get(index));
            index++;
        } else if (amountTokens.isEmpty() && index < tokens.size() && isUnitToken(tokens.get(index))) {
            unit = normalizeDisplayUnit(tokens.get(index));
            amountTokens.add("1");
            index++;
        }

        String amount = amountTokens.isEmpty() ? "1" : String.join(" ", amountTokens);
        String ingredientName = index >= tokens.size()
                ? line.trim()
                : String.join(" ", tokens.subList(index, tokens.size())).trim();

        if (ingredientName.isEmpty()) {
            ingredientName = line.trim();
        }

        return new IngredientEntry(capitalizeWords(ingredientName), amount, unit);
    }

    private void splitCombinedQuantityAndUnit(List<String> tokens) {
        Matcher matcher = QUANTITY_AND_UNIT_PATTERN.matcher(tokens.get(0).replace(",", "."));
        if (matcher.matches() && isUnitToken(matcher.group(2))) {
            tokens.set(0, matcher.group(1));
            tokens.add(1, matcher.group(2));
        }
    }

    public List<String> collectIngredientSuggestions(Iterable<Recipe> recipes,
                                                     Iterable<Favorite> favorites,
                                                     Iterable<ShoppingItem> shoppingItems) {
        Set<String> suggestions = new LinkedHashSet<>();

        for (Recipe recipe : recipes) {
            parseIngredients(recipe.getIngredients()).forEach(entry -> suggestions.add(entry.ingredientName()));
        }
        for (Favorite favorite : favorites) {
            String ingredients = favorite.getRecipe() != null ? favorite.getRecipe().getIngredients() : favorite.getRecipeIngredients();
            parseIngredients(ingredients).forEach(entry -> suggestions.add(entry.ingredientName()));
        }
        for (ShoppingItem shoppingItem : shoppingItems) {
            if (shoppingItem.getIngredientName() != null && !shoppingItem.getIngredientName().isBlank()) {
                suggestions.add(capitalizeWords(shoppingItem.getIngredientName().trim()));
            }
        }

        return new ArrayList<>(suggestions);
    }

    public List<String> collectUnitSuggestions(Iterable<Recipe> recipes,
                                               Iterable<Favorite> favorites,
                                               Iterable<ShoppingItem> shoppingItems) {
        Set<String> suggestions = new LinkedHashSet<>(List.of(
                "item", "cup", "tbsp", "tsp", "g", "kg", "ml", "l",
                "pinch", "clove", "slice", "can", "packet", "piece"
        ));

        for (Recipe recipe : recipes) {
            parseIngredients(recipe.getIngredients()).forEach(entry -> suggestions.add(entry.unit()));
        }
        for (Favorite favorite : favorites) {
            String ingredients = favorite.getRecipe() != null ? favorite.getRecipe().getIngredients() : favorite.getRecipeIngredients();
            parseIngredients(ingredients).forEach(entry -> suggestions.add(entry.unit()));
        }
        for (ShoppingItem shoppingItem : shoppingItems) {
            if (shoppingItem.getUnit() != null && !shoppingItem.getUnit().isBlank()) {
                suggestions.add(normalizeDisplayUnit(shoppingItem.getUnit()));
            }
        }

        return new ArrayList<>(suggestions);
    }

    private boolean isQuantityToken(String token) {
        String cleaned = normalizeToken(token);
        return cleaned.matches("\\d+")
                || cleaned.matches("\\d+/\\d+")
                || cleaned.matches("\\d+[.,]\\d+")
                || cleaned.matches("\\d+-\\d+")
                || cleaned.matches("[¼½¾]");
    }

    private boolean isUnitToken(String token) {
        return UNIT_WORDS.contains(normalizeDisplayUnit(token));
    }

    private String normalizeDisplayUnit(String token) {
        if (token == null || token.isBlank()) {
            return "item";
        }
        String normalized = normalizeToken(token).toLowerCase(Locale.ROOT);
        return UNIT_ALIASES.getOrDefault(normalized, normalized);
    }

    private String normalizeToken(String token) {
        return token.replace(",", "").replace(".", "");
    }

    private String capitalizeWords(String text) {
        String[] words = text.toLowerCase(Locale.ROOT).split("\\s+");
        List<String> capitalized = new ArrayList<>();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
            }
        }
        return String.join(" ", capitalized);
    }

    private BigDecimal parseAmountValue(String amount) {
        if (amount == null || amount.isBlank()) {
            return BigDecimal.ONE;
        }

        String value = amount.trim().replace(",", ".");
        if (value.contains("/")) {
            String[] parts = value.split("/");
            if (parts.length == 2) {
                return new BigDecimal(parts[0]).divide(new BigDecimal(parts[1]), 6, RoundingMode.HALF_UP);
            }
        }
        return new BigDecimal(value);
    }

    private String toAggregationGroupUnit(String unit) {
        return switch (normalizeDisplayUnit(unit)) {
            case "l", "ml" -> "ml";
            case "kg", "g", "mg" -> "g";
            default -> normalizeDisplayUnit(unit);
        };
    }

    private BigDecimal convertToGroupUnit(BigDecimal value, String originalUnit, String groupUnit) {
        String normalizedUnit = normalizeDisplayUnit(originalUnit);
        if ("ml".equals(groupUnit)) {
            return switch (normalizedUnit) {
                case "l" -> value.multiply(BigDecimal.valueOf(1000));
                default -> value;
            };
        }
        if ("g".equals(groupUnit)) {
            return switch (normalizedUnit) {
                case "kg" -> value.multiply(BigDecimal.valueOf(1000));
                case "mg" -> value.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
                default -> value;
            };
        }
        return value;
    }

    private static class AggregateBucket {
        private final String ingredientName;
        private final String groupUnit;
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private boolean purchased;

        private AggregateBucket(String ingredientName, String groupUnit) {
            this.ingredientName = ingredientName;
            this.groupUnit = groupUnit;
        }

        private void add(BigDecimal amount, boolean purchased) {
            totalAmount = totalAmount.add(amount);
            this.purchased = this.purchased || purchased;
        }

        private String ingredientName() {
            return ingredientName;
        }

        private String finalUnit() {
            if ("ml".equals(groupUnit) && totalAmount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                return "l";
            }
            if ("g".equals(groupUnit) && totalAmount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                return "kg";
            }
            return groupUnit;
        }

        private String finalAmount() {
            BigDecimal displayAmount = totalAmount;
            if ("ml".equals(groupUnit) && totalAmount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                displayAmount = totalAmount.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            } else if ("g".equals(groupUnit) && totalAmount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                displayAmount = totalAmount.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            }
            return displayAmount.stripTrailingZeros().toPlainString();
        }

        private boolean purchased() {
            return purchased;
        }
    }

    public record IngredientEntry(String ingredientName, String amount, String unit) {
    }
}
