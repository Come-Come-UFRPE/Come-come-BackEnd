package com.historico_Come_Come.exceptions;

public class HistoryNotFoundException extends RuntimeException {
    public HistoryNotFoundException() { super("Histórico não encontrado!");
    }

    public HistoryNotFoundException(String message) {
        super(message);
    }
}
