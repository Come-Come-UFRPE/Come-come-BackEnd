package com.comecome.anamnese.dtos;

import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.models.enums.*;

import java.util.Set;

public record AnamneseResponseDTO(Double peso,
                                  Double altura,
                                  Integer idade,
                                  Gender sexo,
                                  Objective objective,
                                  Set<FoodAllergy> foodAllergies,
                                  Set<HealthCondition> healthConditions,
                                  Set<Diet> diets) {

    public AnamneseResponseDTO(Anamnese anamnese) {
        this(
                anamnese.getPeso(),
                anamnese.getAltura(),
                anamnese.getIdade(),
                anamnese.getSexo(),
                anamnese.getObjective(),
                anamnese.getFoodAllergy(),
                anamnese.getHealthCondition(),
                anamnese.getDiet()
        );
    }
}