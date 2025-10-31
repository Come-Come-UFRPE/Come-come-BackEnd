package com.comecome.openfoodfacts.dtos.responseDtos;

import java.util.List;

public record IngredientDto(
        String id,
        String percent_estimate,
        String text,
        String vegan,
        String vegetarian,
        List<IngredientDto> ingredients
) {}