package com.comecome.cadastro.exceptions;

public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException() { super("Usuário não autenticado!");}

    public NotAuthenticatedException(String message) {
        super(message);
    }
}
