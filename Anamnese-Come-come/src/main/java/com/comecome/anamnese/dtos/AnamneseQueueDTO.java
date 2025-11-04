package com.comecome.anamnese.dtos;

import java.util.UUID;

public class AnamneseQueueDTO {

    private UUID id;
    private String evento;

    public AnamneseQueueDTO(UUID id) {
        this.id = id;
        this.evento = "User ID inserido";
    }
}
