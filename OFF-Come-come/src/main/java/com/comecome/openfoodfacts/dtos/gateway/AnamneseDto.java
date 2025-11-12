package com.comecome.openfoodfacts.dtos.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AnamneseDto {

    private UUID userID;
    private Double peso;
    private Double altura;
    private Integer idade;
    private String sexo; // Gender enum como String

    // Mapeamos os Enums como Set<String> para facilitar a leitura do JSON
    @JsonProperty("healthCondition")
    private Set<String> healthCondition = new HashSet<>();

    @JsonProperty("foodAllergy")
    private Set<String> foodAllergy = new HashSet<>();

    @JsonProperty("diet")
    private Set<String> diet = new HashSet<>();

    @JsonProperty("objective")
    private Set<String> objective = new HashSet<>();


    // --- Métodos Auxiliares para verificação ---
    public boolean temCondicao(String condicao) {
        return healthCondition != null && healthCondition.contains(condicao);
    }

    public boolean temDieta(String dieta) {
        return diet != null && diet.contains(dieta);
    }

    public boolean temObjetivo(String objetivo) {
        return objective != null && objective.contains(objetivo);
    }

    // Verifica se possui uma alergia específica (ex: "GLUTEN")
    public boolean temAlergia(String alergia) {
        return foodAllergy != null && foodAllergy.contains(alergia);
    }

    //Getters e setters

    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
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

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Set<String> getHealthCondition() {
        return healthCondition;
    }

    public void setHealthCondition(Set<String> healthCondition) {
        this.healthCondition = healthCondition;
    }

    public Set<String> getFoodAllergy() {
        return foodAllergy;
    }

    public void setFoodAllergy(Set<String> foodAllergy) {
        this.foodAllergy = foodAllergy;
    }

    public Set<String> getDiet() {
        return diet;
    }

    public void setDiet(Set<String> diet) {
        this.diet = diet;
    }

    public Set<String> getObjective() {
        return objective;
    }

    public void setObjective(Set<String> objective) {
        this.objective = objective;
    }
}