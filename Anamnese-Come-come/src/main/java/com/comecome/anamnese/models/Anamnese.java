package com.comecome.anamnese.models;

import com.comecome.anamnese.models.enums.*;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ms-anamnese")
public class Anamnese {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "anamnese_id")
    private UUID anamneseID;

    //Dados básicos como peso, altura, idade e sexo biológico(podemos tirar futuramente?)
    private Double peso;
    private Double altura;
    private Integer idade;

    @Enumerated(EnumType.STRING)
    private Gender sexo;

    @Enumerated(EnumType.STRING)
    private Objective objective;


    // # Relacionamento One-to-Many como alergias #
    // Alergias do Usuário
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "anamnese_alergias",
            joinColumns = @JoinColumn(name = "anamnese_id"),
            indexes = @Index(name = "idx_anamnese_alergias", columnList = "anamnese_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "alergias")
    private Set<FoodAllergy> foodAllergy = new HashSet<>();


    // Condições de Saúde do Usuário
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "anamnese_condicao_saude",
            joinColumns = @JoinColumn(name = "anamnese_id"),
            indexes = @Index(name = "idx_anamnese_condicao_saude", columnList = "anamnese_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "condicao_saude")
    private Set<HealthCondition> healthCondition = new HashSet<>();


    // Dietas do usuário
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "anamnese_dieta",
            joinColumns = @JoinColumn(name = "anamnese_id"),
            indexes = @Index(name = "idx_anamnese_dieta", columnList = "anamnese_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "dietas")
    private Set<Diet> diet = new HashSet<>();



    public UUID getAnamneseID() {
        return anamneseID;
    }

    public void setAnamneseID(UUID anamneseID) {
        this.anamneseID = anamneseID;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Gender getSexo() {
        return sexo;
    }

    public void setSexo(Gender sexo) {
        this.sexo = sexo;
    }

    public Objective getObjective() {
        return objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    // # Alergias #
    public Set<FoodAllergy> getFoodAllergy() {
        return foodAllergy;
    }

    public void setFoodAllergy(Set<FoodAllergy> foodAllergy) {
        this.foodAllergy = foodAllergy;
    }

    public void addFoodAllergy(FoodAllergy foodAllergy){
        if (foodAllergy != null) {
            this.foodAllergy.add(foodAllergy);
        }
    }

    public void removeFoodAllergy(FoodAllergy foodAllergy){
        if (foodAllergy != null){
            this.foodAllergy.remove(foodAllergy);
        }
    }

    public boolean boolFoodAllergy(FoodAllergy foodAllergy) {
        return this.foodAllergy.contains(foodAllergy);
    }

    public void clearFoodAllergy() {
        this.foodAllergy.clear();
    }



    // # Condições de Saúde #
    public Set<HealthCondition> getHealthCondition() {
        return healthCondition;
    }

    public void setHealthCondition(Set<HealthCondition> healthCondition) {
        this.healthCondition = healthCondition;
    }

    public void addHealthCondition(HealthCondition healthCondition){
        if (healthCondition != null) {
            this.healthCondition.add(healthCondition);
        }
    }

    public void removeHealthCondition(HealthCondition healthCondition){
        if (healthCondition != null) {
            this.healthCondition.remove(healthCondition);
        }
    }

    public boolean boolHealthCondition(HealthCondition healthCondition) {
        return this.healthCondition.contains(healthCondition);
    }

    public void clearHealthCondition() {
        this.healthCondition.clear();
    }




    // # Dietas #
    public Set<Diet> getDiet() {
        return diet;
    }

    public void setDiet(Set<Diet> diet) {
        this.diet = diet;
    }

    public void addDiet(Diet diet){
        if (diet != null) {
            this.diet.add(diet);
        }
    }

    public void removeDiet(Diet diet){
        if (diet != null) {
            this.diet.remove(diet);
        }
    }

    public boolean boolDiet(Diet diet) {
        return this.diet.contains(diet);
    }

    public void clearDiet() {
        this.diet.clear();
    }


    // # Funções Auxiliares #

    public Double calcularIMC() {
        if (peso != null && altura != null && altura > 0) {
            return peso / (altura * altura);
        }
        return null;
    }
}
