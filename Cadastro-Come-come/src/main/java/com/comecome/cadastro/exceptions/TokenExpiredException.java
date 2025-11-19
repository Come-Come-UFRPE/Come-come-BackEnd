package com.comecome.cadastro.exceptions;

import org.springframework.security.core.AuthenticationException;

public class TokenExpiredException extends AuthenticationException {
    public TokenExpiredException() { super("Token JWT expirado."); }

    public TokenExpiredException(String message){
        super(message);
    }
}