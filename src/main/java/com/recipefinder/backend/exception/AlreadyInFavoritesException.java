package com.recipefinder.backend.exception;

public class AlreadyInFavoritesException extends Exception {
    public AlreadyInFavoritesException() {
        super("This recipe is already in your favorites list!");
    }
}
