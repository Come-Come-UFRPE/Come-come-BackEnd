package com.comecome.anamnese.dtos;

import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.models.enums.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record AnamneseRecordDTO( UUID userID,
                                @Positive(message = "Peso deve ser positivo")
                                @Max(value = 500, message = "Peso não pode exceder 500kg")
                                Double peso,

                                @Positive(message = "Altura deve ser positiva")
                                @Max(value = 400, message = "Altura não pode exceder 400cm")
                                Integer altura,

                                @Min(value = 0, message = "Idade não pode ser negativa")
                                @Max(value = 150, message = "Idade não pode exceder 150 anos")
                                Integer idade,

                                @NotNull(message = "Sexo é obrigatório")
                                Gender sexo,

                                @NotNull(message = "Objetivo é obrigatório")
                                Objective objective,

                                Set<FoodAllergy> foodAllergy,

                                Set<HealthCondition> healthCondition,

                                Set<Diet> diet) {
    public Anamnese toEntity() {
        Anamnese anamnese = new Anamnese();
        anamnese.setUserID(this.userID);
        anamnese.setPeso(this.peso);
        anamnese.setAltura(this.altura);
        anamnese.setIdade(this.idade);
        anamnese.setSexo(this.sexo);
        anamnese.setObjective(this.objective);

        // Copia as collections manualmente
        if (this.foodAllergy != null) {
            anamnese.setFoodAllergy(new HashSet<>(this.foodAllergy));
        }

        if (this.healthCondition != null) {
            anamnese.setHealthCondition(new HashSet<>(this.healthCondition));
        }

        if (this.diet != null) {
            anamnese.setDiet(new HashSet<>(this.diet));
        }

        return anamnese;
    }
}
