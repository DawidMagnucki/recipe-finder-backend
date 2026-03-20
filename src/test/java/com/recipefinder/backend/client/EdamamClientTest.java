package com.recipefinder.backend.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdamamClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EdamamClient edamamClient;

    @Test
    void shouldReturnCaloriesFromApiResponse() {
        ReflectionTestUtils.setField(edamamClient, "appId", "id");
        ReflectionTestUtils.setField(edamamClient, "appKey", "key");
        Map<String, Object> response = Map.of(
                "parsed", List.of(
                        Map.of("food", Map.of("nutrients", Map.of("ENERC_KCAL", 123.7)))
                )
        );
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class))).thenReturn(response);

        Integer calories = edamamClient.getCaloriesForIngredient("apple");

        assertEquals(123, calories);
    }

    @Test
    void shouldReturnZeroWhenResponseIsMissingParsedData() {
        ReflectionTestUtils.setField(edamamClient, "appId", "id");
        ReflectionTestUtils.setField(edamamClient, "appKey", "key");
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class))).thenReturn(Map.of());

        Integer calories = edamamClient.getCaloriesForIngredient("apple");

        assertEquals(0, calories);
    }

    @Test
    void shouldReturnZeroWhenApiThrowsException() {
        ReflectionTestUtils.setField(edamamClient, "appId", "id");
        ReflectionTestUtils.setField(edamamClient, "appKey", "key");
        when(restTemplate.getForObject(anyString(), org.mockito.ArgumentMatchers.eq(Map.class)))
                .thenThrow(new RuntimeException("boom"));

        Integer calories = edamamClient.getCaloriesForIngredient("apple");

        assertEquals(0, calories);
    }
}
