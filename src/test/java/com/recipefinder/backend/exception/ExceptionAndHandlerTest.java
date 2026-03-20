package com.recipefinder.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionAndHandlerTest {

    @Test
    void shouldExposeRecipeNotFoundMessage() {
        RecipeNotFoundException exception = new RecipeNotFoundException(5L);

        assertEquals("Recipe with ID 5 was not found in our database.", exception.getMessage());
    }

    @Test
    void shouldExposeFavoriteNotFoundMessage() {
        FavoriteNotFoundException exception = new FavoriteNotFoundException(8L);

        assertEquals("Favorite record with ID 8 was not found.", exception.getMessage());
    }

    @Test
    void shouldExposeShoppingItemNotFoundMessage() {
        ShoppingItemNotFoundException exception = new ShoppingItemNotFoundException(4L);

        assertEquals("Shopping item with ID 4 does not exist on your list.", exception.getMessage());
    }

    @Test
    void shouldExposeAlreadyInFavoritesMessage() {
        AlreadyInFavoritesException exception = new AlreadyInFavoritesException();

        assertEquals("This recipe is already in your favorites list!", exception.getMessage());
    }

    @Test
    void shouldHandleRecipeNotFoundException() {
        GlobalHttpErrorHandler handler = new GlobalHttpErrorHandler();

        ResponseEntity<Object> response = handler.handleRecipeNotFoundException(new RecipeNotFoundException(3L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recipe with ID 3 was not found in our database.", response.getBody());
    }

    @Test
    void shouldHandleAlreadyInFavoritesException() {
        GlobalHttpErrorHandler handler = new GlobalHttpErrorHandler();

        ResponseEntity<Object> response = handler.handleAlreadyInFavoritesException(new AlreadyInFavoritesException());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("This recipe is already in your favorites list!", response.getBody());
    }
}
