package com.comecome.cadastro.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException() { super("Token JWT inválido."); }

    public InvalidTokenException(String message){
        super(message);
    }
}
