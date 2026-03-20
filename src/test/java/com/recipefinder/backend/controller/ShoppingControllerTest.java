package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.ShoppingItem;
import com.recipefinder.backend.dto.ShoppingItemDto;
import com.recipefinder.backend.mapper.ShoppingMapper;
import com.recipefinder.backend.repository.ShoppingRepository;
import com.recipefinder.backend.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingController.class)
@Import(ShoppingMapper.class)
@ActiveProfiles("test")
class ShoppingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private ShoppingRepository shoppingRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditService auditService;

    @Test
    void shouldReturnShoppingItems() throws Exception {
        ShoppingItem item = ShoppingItem.builder()
                .id(1L)
                .ingredientName("Milk")
                .amount("200")
                .unit("ml")
                .isPurchased(false)
                .build();
        when(shoppingRepository.findAll()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/shopping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredientName").value("Milk"))
                .andExpect(jsonPath("$[0].amount").value("200"))
                .andExpect(jsonPath("$[0].unit").value("ml"));
    }

    @Test
    void shouldAddShoppingItem() throws Exception {
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .ingredientName("Sugar")
                .amount("2")
                .unit("cup")
                .isPurchased(false)
                .build();
        ShoppingItem saved = ShoppingItem.builder()
                .id(5L)
                .ingredientName("Sugar")
                .amount("2")
                .unit("cup")
                .isPurchased(false)
                .build();
        when(shoppingRepository.save(any(ShoppingItem.class))).thenReturn(saved);

        mockMvc.perform(post("/api/shopping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.ingredientName").value("Sugar"))
                .andExpect(jsonPath("$.unit").value("cup"));

        verify(auditService).log("API_ADD_SHOPPING_ITEM", "Added: Sugar");
    }

    @Test
    void shouldTogglePurchasedStatus() throws Exception {
        ShoppingItem item = ShoppingItem.builder()
                .id(9L)
                .ingredientName("Flour")
                .amount("500")
                .unit("g")
                .isPurchased(false)
                .build();
        ShoppingItem saved = ShoppingItem.builder()
                .id(9L)
                .ingredientName("Flour")
                .amount("500")
                .unit("g")
                .isPurchased(true)
                .build();
        when(shoppingRepository.findById(9L)).thenReturn(Optional.of(item));
        when(shoppingRepository.save(any(ShoppingItem.class))).thenReturn(saved);

        mockMvc.perform(put("/api/shopping/9/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchased").value(true));

        verify(auditService).log("API_TOGGLE_SHOPPING_ITEM", "Toggled status for: Flour");
    }

    @Test
    void shouldDeleteShoppingItem() throws Exception {
        when(shoppingRepository.existsById(7L)).thenReturn(true);

        mockMvc.perform(delete("/api/shopping/7"))
                .andExpect(status().isOk());

        verify(shoppingRepository).deleteById(7L);
        verify(auditService).log("API_DELETE_SHOPPING_ITEM", "Deleted item ID: 7");
    }
}
