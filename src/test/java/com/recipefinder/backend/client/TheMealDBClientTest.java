package com.recipefinder.backend.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TheMealDBClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TheMealDBClient theMealDBClient;

    @Test
    void shouldFetchRandomMeal() {
        MealResponse response = new MealResponse();
        response.setMeals(List.of(Map.of("idMeal", "1", "strMeal", "Pasta")));
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(MealResponse.class))).thenReturn(response);

        Map<String, String> meal = theMealDBClient.fetchRandomMeal();

        assertEquals("Pasta", meal.get("strMeal"));
    }

    @Test
    void shouldReturnNullWhenNoRandomMealReturned() {
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(MealResponse.class))).thenReturn(null);

        Map<String, String> meal = theMealDBClient.fetchRandomMeal();

        assertNull(meal);
    }

    @Test
    void shouldFetchRandomMealByCategory() {
        MealResponse listResponse = new MealResponse();
        listResponse.setMeals(List.of(Map.of("idMeal", "99")));
        MealResponse detailResponse = new MealResponse();
        detailResponse.setMeals(List.of(Map.of("idMeal", "99", "strMeal", "Curry")));

        when(restTemplate.getForObject(org.mockito.ArgumentMatchers.contains("filter.php?c=Breakfast"), org.mockito.ArgumentMatchers.eq(MealResponse.class)))
                .thenReturn(listResponse);
        when(restTemplate.getForObject(org.mockito.ArgumentMatchers.contains("lookup.php?i=99"), org.mockito.ArgumentMatchers.eq(MealResponse.class)))
                .thenReturn(detailResponse);

        Map<String, String> meal = theMealDBClient.fetchRandomMealByCategory("Breakfast");

        assertEquals("Curry", meal.get("strMeal"));
    }

    @Test
    void shouldReturnNullWhenCategoryListIsEmpty() {
        MealResponse response = new MealResponse();
        response.setMeals(List.of());
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(MealResponse.class))).thenReturn(response);

        Map<String, String> meal = theMealDBClient.fetchRandomMealByCategory("Breakfast");

        assertNull(meal);
    }
}
