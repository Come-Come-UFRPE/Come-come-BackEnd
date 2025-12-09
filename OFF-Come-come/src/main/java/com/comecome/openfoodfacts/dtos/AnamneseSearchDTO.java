package com.comecome.openfoodfacts.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnamneseSearchDTO {
    private UUID userID;
    private String query;
}
