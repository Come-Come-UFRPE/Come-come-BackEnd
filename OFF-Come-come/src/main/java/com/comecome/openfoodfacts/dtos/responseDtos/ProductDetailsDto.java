package com.comecome.openfoodfacts.dtos.responseDtos;

import java.util.List;
import java.util.Map;

public record ProductDetailsDto(
        List<String> allergens,
        List<IngredientDto> ingredients,
        NutrientLevelsDto nutrientLevels,
        Map<String, Object> nutriments,
        String nutritionGrade,
        String veganStatus,
        String vegetarianStatus,
        Integer novaGroup
) {}