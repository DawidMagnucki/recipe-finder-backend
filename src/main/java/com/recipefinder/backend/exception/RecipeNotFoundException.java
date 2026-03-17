package com.recipefinder.backend.exception;

public class RecipeNotFoundException extends Exception {
    public RecipeNotFoundException(Long id) {
        super("Recipe with ID " + id + " was not found in our database.");
    }
}
