package com.comecome.anamnese.exceptions;

public class AnamneseNotFoundException extends RuntimeException{
    public AnamneseNotFoundException() {
        super("Anamnese não encontrada!");
    }

    public AnamneseNotFoundException(String message) {
        super(message);
    }
}
