package com.comecome.openfoodfacts.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnamneseResponseDTO {
    private UUID userID;
    private String query;
    private LocalDateTime dataDaBusca;
}
