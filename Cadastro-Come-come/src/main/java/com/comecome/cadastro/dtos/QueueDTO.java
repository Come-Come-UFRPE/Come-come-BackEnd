package com.comecome.cadastro.dtos;

import java.util.UUID;

public class QueueDTO {

    private UUID id;
    private String evento;

    public QueueDTO(UUID id) {
        this.id = id;
        this.evento = "Usuário criado";
    }
}
