package com.comecome.openfoodfacts.dtos.responseDtos;

public record ProductResponseDto(
        String name,
        String image,
        ProductDetailsDto details
) {}