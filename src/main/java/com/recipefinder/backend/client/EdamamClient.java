package com.recipefinder.backend.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EdamamClient {
    private final RestTemplate restTemplate;

    @Value("${edamam.api.app-id}")
    private String appId;

    @Value("${edamam.api.app-key}")
    private String appKey;

    public static final String BASE_URL = "https://api.edamam.com/api/food-database/v2/parser";

    public Integer getCaloriesForIngredient(String ingredient) {
//        String url = String.format("%s?q=%s&app_id=%s&app_key=%s", BASE_URL, ingredient, appId, appKey);
        String url = BASE_URL + "?app_id=" + appId + "&app_key=" + appKey + "&ingr=" + ingredient;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("parsed")) {
                List<Map<String, Object>> parsed = (List<Map<String, Object>>) response.get("parsed");
                if (!parsed.isEmpty()) {
                    Map<String, Object> food = (Map<String, Object>) parsed.get(0).get("food");
                    Map<String, Object> nutrients = (Map<String, Object>) food.get("nutrients");
                    Number kcal = (Number) nutrients.get("ENERC_KCAL");
                    return kcal.intValue();
                }
            }
        } catch (Exception e) {
            System.err.println("Edamam API error: " + e.getMessage());
        }
        return 0;
    }

}
