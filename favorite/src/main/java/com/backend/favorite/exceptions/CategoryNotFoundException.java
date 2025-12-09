package com.backend.favorite.exceptions;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException() {
        super("Categoria não encontrada no banco de dados!");
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
