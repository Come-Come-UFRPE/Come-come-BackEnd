package com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs;

import com.comecome.openfoodfacts.dtos.responseDtos.IngredientDto;
import com.comecome.openfoodfacts.dtos.responseDtos.NutrientLevelsDto;

import java.util.List;
import java.util.Map;

public record NewProductDetailsDTO (
    List<String> allergens,
    List<NewIngredientDTO> ingredients,
    Map<String, Object> nutriments,
    NutrientLevelsDto nutrient_levels,
    List<String> ingredient_tags,
    String nutrition_grade_fr
){}
