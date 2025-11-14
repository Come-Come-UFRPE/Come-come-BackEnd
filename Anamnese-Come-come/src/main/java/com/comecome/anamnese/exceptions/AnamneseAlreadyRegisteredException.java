package com.comecome.anamnese.exceptions;

public class AnamneseAlreadyRegisteredException extends RuntimeException {
    public AnamneseAlreadyRegisteredException() { super("Anamnese já registrada!");
    }

    public AnamneseAlreadyRegisteredException(String message) {
        super(message);
    }
}
