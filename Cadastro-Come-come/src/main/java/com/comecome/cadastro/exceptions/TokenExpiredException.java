package com.comecome.cadastro.exceptions;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() { super("Token JWT expirado."); }

    public TokenExpiredException(String message){
        super(message);
    }
}