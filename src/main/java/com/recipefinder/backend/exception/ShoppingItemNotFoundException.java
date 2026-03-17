package com.recipefinder.backend.exception;

public class ShoppingItemNotFoundException extends Exception {
    public ShoppingItemNotFoundException(Long id) {
        super("Shopping item with ID " + id + " does not exist on your list.");
    }
}
