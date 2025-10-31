package com.comecome.cadastro.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRecordDto(@NotBlank String name,
                            @NotBlank @Email String email,
                            @NotBlank String password,
                            @NotBlank String estado,
                            @NotBlank String cidade,
                            @NotNull @Min(value = 0) Integer idade) {

}
