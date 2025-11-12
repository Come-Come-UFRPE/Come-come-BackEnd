package com.comecome.openfoodfacts.dtos.responseDtos;

import java.util.List;
import java.util.Map;

public record ProductDetailsDto(
        List<String> allergens,
        List<IngredientDto> ingredients,
        NutrientLevelsDto nutrient_levels,
        Map<String, Object> nutriments,
        List<String> ingredient_tags,
        String nutrition_grade_fr
) {}