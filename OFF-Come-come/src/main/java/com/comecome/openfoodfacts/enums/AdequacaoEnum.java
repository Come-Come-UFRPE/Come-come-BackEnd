package com.comecome.openfoodfacts.enums;

public enum AdequacaoEnum {
    VERDE(1),
    AMARELO(2),
    VERMELHO(3);

    private final int peso;

    AdequacaoEnum(int peso) { this.peso = peso; }

    public int getPeso() { return peso; }
}