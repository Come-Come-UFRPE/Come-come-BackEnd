package com.comecome.openfoodfacts.dtos.responseDtos;


public record ComparisonResponseDto(
        SortingItemDto productA,
        SortingItemDto productB
) {}