package com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs;

import java.util.List;
import java.util.Map;

public record NewProductDetailsDTO (
    List<String> allergens,
    List<NewIngredientDTO> ingredients,
    Map<String, Object> nutriments,
    List<String> ingredient_tags,
    String nutrition_grade_fr
){}
