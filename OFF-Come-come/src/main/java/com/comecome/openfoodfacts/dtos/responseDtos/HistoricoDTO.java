package com.comecome.openfoodfacts.dtos.responseDtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoDTO(UUID userId, String text, LocalDateTime dataDaBusca) {
}
