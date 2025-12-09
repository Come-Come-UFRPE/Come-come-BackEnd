package com.backend.favorite.exceptions;

public class CategoryAlreadyAddedException extends RuntimeException{
    public CategoryAlreadyAddedException() {
        super("Nome de Categoria já criada! Crie uma categoria com outro nome não repetido");
    }

    public CategoryAlreadyAddedException(String message) {
        super(message);
    }
}
