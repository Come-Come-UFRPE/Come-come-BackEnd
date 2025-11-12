package com.historico_Come_Come.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoryRecord(
        UUID userID,
        String query,
        LocalDateTime dataDaBusca
) {}
