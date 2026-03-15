package com.recipefinder.backend.client;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MealResponse {
    private List<Map<String, String>> meals;
}
