package com.recipefinder.backend.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class TheMealDBClient {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://www.themealdb.com";
    private static final String RANDOM_MEAL_URL = "https://www.themealdb.com/api/json/v1/1/random.php";
    private static final String FILTER_BY_CATEGORY_URL = "https://www.themealdb.com/api/json/v1/1/filter.php?c=%s";
    private static final String LOOKUP_BY_ID_URL = "https://www.themealdb.com/api/json/v1/1/lookup.php?i=%s";

    public Map<String, String> fetchRandomMeal() {
        MealResponse response = restTemplate.getForObject(RANDOM_MEAL_URL, MealResponse.class);

        if (response != null && response.getMeals() != null && !response.getMeals().isEmpty()) {
            return response.getMeals().get(0);
        }
        return null;
    }

    public Map<String, String> fetchRandomMealByCategory(String category) {
        String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
        String categoryUrl = String.format(FILTER_BY_CATEGORY_URL, encodedCategory);
        MealResponse listResponse = restTemplate.getForObject(categoryUrl, MealResponse.class);

        if (listResponse != null && listResponse.getMeals() != null && !listResponse.getMeals().isEmpty()) {
            List<Map<String, String>> meals = listResponse.getMeals();
            int randomIndex = ThreadLocalRandom.current().nextInt(meals.size());
            String mealId = meals.get(randomIndex).get("idMeal");

            String lookupUrl = String.format(LOOKUP_BY_ID_URL, mealId);
            MealResponse detailResponse = restTemplate.getForObject(lookupUrl, MealResponse.class);

            if (detailResponse != null && detailResponse.getMeals() != null && !detailResponse.getMeals().isEmpty()) {
                return detailResponse.getMeals().get(0);
            }
        }
        return null;
    }
}
