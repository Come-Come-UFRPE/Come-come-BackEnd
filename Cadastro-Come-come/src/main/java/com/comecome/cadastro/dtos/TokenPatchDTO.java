package com.comecome.cadastro.dtos;

public record TokenPatchDTO (String email,
                             String token,
                             String newPassword ){
}
