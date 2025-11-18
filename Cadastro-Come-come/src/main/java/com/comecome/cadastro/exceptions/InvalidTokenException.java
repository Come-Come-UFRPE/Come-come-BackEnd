package com.comecome.cadastro.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() { super("Token JWT inválido."); }

    public InvalidTokenException(String message){
        super(message);
    }
}
