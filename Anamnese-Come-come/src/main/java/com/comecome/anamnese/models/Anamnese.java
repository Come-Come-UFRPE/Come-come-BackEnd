package com.comecome.anamnese.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ms-anamnese")
public class Anamnese {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID anamneseId;

    private double peso;
    private double altura;
    private int idade;

    public Anamnese(UUID anamneseId, double peso, double altura, int idade) {
        this.anamneseId = anamneseId;
        this.peso = peso;
        this.altura = altura;
        this.idade = idade;
    }


    public UUID getAnamneseId() {
        return anamneseId;
    }

    public void setAnamneseId(UUID anamneseId) {
        this.anamneseId = anamneseId;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }
}
