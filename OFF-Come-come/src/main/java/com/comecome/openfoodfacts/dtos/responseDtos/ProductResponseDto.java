package com.comecome.openfoodfacts.dtos.responseDtos;

import java.util.List;

public record ProductResponseDto(
        String name,
        String image,
        ProductDetailsDto details,
        List<String> violations
) {}