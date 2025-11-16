package com.comecome.openfoodfacts.exceptions;

public class FoodNotFoundException extends RuntimeException {
    public FoodNotFoundException() {
        super("Alimento não encontrado!");
    }

    public FoodNotFoundException(String message) {
        super(message);
    }
}
