package com.comecome.openfoodfacts.dtos;

import com.comecome.openfoodfacts.models.enums.Diet;
import com.comecome.openfoodfacts.models.enums.FoodAllergy;
import com.comecome.openfoodfacts.models.enums.HealthCondition;
import com.comecome.openfoodfacts.models.enums.Objective;

import java.util.Set;

public record AnamnesePatchDto(Objective objective,
                               Set<FoodAllergy> foodAllergy,
                               Set<HealthCondition> healthCondition,
                               Set<Diet> diet) {
}
