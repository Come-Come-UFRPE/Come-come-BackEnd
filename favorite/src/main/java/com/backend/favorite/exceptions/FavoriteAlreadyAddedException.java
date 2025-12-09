package com.backend.favorite.exceptions;

public class FavoriteAlreadyAddedException extends RuntimeException{
    public FavoriteAlreadyAddedException() {
        super("Favorito já adicionado na Lista!");
    }

    public FavoriteAlreadyAddedException(String message) {
        super(message);
    }
}
