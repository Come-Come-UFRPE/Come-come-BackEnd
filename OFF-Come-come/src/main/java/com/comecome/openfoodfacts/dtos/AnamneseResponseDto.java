package com.comecome.openfoodfacts.dtos;

import com.comecome.openfoodfacts.models.enums.*;

import java.util.Set;
import java.util.UUID;

public record AnamneseResponseDto(UUID anamneseID,
                                  UUID userID,
                                  Double peso,
                                  Integer altura,
                                  Integer idade,
                                  Gender sexo,
                                  Objective objective,
                                  Set<FoodAllergy> foodAllergies,
                                  Set<HealthCondition> healthConditions,
                                  Set<Diet> diets,
                                  Double imc) {
}