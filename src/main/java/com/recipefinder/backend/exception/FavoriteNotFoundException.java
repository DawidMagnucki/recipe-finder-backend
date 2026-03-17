package com.recipefinder.backend.exception;

public class FavoriteNotFoundException extends Exception {
    public FavoriteNotFoundException(Long id) {
        super("Favorite record with ID " + id + " was not found.");
    }
}
