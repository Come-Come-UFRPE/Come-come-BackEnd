package com.backend.favorite.exceptions;

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException() {
        super("Favorito não encontrado no banco de dados!");
    }

    public FavoriteNotFoundException(String message) {
        super(message);
    }
}
