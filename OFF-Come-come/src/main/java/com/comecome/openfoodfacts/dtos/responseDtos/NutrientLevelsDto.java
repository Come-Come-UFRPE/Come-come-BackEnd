package com.comecome.openfoodfacts.dtos.responseDtos;

public record NutrientLevelsDto(
        String fat,
        String salt,
        String saturated_fat,
        String sugars
) {}