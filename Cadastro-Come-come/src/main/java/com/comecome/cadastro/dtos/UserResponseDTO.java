package com.comecome.cadastro.dtos;

import com.comecome.cadastro.models.User;

import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String name,
                              String email,
                              String cidade,
                              String estado,
                              int idade,
                              boolean fezAnamnese) {

    public UserResponseDTO(User user) {
        this(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getCidade(),
                user.getEstado(),
                user.getIdade(),
                user.isFezAnamnese()
        );
    }
}