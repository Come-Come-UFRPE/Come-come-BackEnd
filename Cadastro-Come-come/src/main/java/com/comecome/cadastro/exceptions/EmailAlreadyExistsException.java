package com.comecome.cadastro.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() { super("Email já cadastrado!");}

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
