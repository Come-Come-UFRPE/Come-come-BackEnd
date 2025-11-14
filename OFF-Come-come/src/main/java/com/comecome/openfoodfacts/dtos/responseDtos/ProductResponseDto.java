package com.comecome.openfoodfacts.dtos.responseDtos;

public record ProductResponseDto(
        String id,
        String name,
        String image,
        ProductDetailsDto details
) {}