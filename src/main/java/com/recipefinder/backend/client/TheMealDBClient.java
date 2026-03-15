package com.recipefinder.backend.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TheMealDBClient {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://www.themealdb.com";
    private static final String RANDOM_MEAL_URL = "https://www.themealdb.com/api/json/v1/1/random.php";
    private static final String MEAL_CATEGORIES_URL = "https://www.themealdb.com/api/json/v1/1/categories.php";

    public Map<String, String> fetchRandomMeal() {
        MealResponse response = restTemplate.getForObject(RANDOM_MEAL_URL, MealResponse.class);

        if (response != null && response.getMeals() != null && !response.getMeals().isEmpty()) {
            return response.getMeals().get(0);
        }
        return null;
    }

    public Map<String, String> fetchRandomMealByCategory(String category) {
        MealResponse listResponse = restTemplate.getForObject(MEAL_CATEGORIES_URL, MealResponse.class);

        if (listResponse != null && listResponse.getMeals() != null && !listResponse.getMeals().isEmpty()) {
            String mealId = listResponse.getMeals().get(0).get("idMeal");

            String lookupUrl = BASE_URL + "lookup.php?i=" + mealId;
            MealResponse detailResponse = restTemplate.getForObject(lookupUrl, MealResponse.class);

            if (detailResponse != null && detailResponse.getMeals() != null && !detailResponse.getMeals().isEmpty()) {
                return detailResponse.getMeals().get(0);
            }
        }
        return null;
    }
}