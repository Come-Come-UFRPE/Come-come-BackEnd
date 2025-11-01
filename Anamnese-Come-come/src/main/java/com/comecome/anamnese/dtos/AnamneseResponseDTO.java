package com.comecome.anamnese.dtos;

import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.models.enums.*;

import java.util.Set;
import java.util.UUID;

public record AnamneseResponseDTO(UUID anamneseID,
                                  Double peso,
                                  Double altura,
                                  Integer idade,
                                  Gender sexo,
                                  Objective objective,
                                  Set<FoodAllergy> foodAllergies,
                                  Set<HealthCondition> healthConditions,
                                  Set<Diet> diets,
                                  Double imc) {

    public AnamneseResponseDTO(Anamnese anamnese) {
        this(
                anamnese.getAnamneseID(),
                anamnese.getPeso(),
                anamnese.getAltura(),
                anamnese.getIdade(),
                anamnese.getSexo(),
                anamnese.getObjective(),
                anamnese.getFoodAllergy(),
                anamnese.getHealthCondition(),
                anamnese.getDiet(),
                anamnese.calcularIMC()
        );
    }

    public static AnamneseResponseDTO fromEntity(Anamnese anamnese) {
        return new AnamneseResponseDTO(
                anamnese.getAnamneseID(),
                anamnese.getPeso(),
                anamnese.getAltura(),
                anamnese.getIdade(),
                anamnese.getSexo(),
                anamnese.getObjective(),
                anamnese.getFoodAllergy(),
                anamnese.getHealthCondition(),
                anamnese.getDiet(),
                anamnese.calcularIMC()
        );
    }
}
