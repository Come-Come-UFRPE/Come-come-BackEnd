package com.comecome.anamnese.services;

import com.comecome.anamnese.dtos.AnamneseResponseDTO;
import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.repositories.AnamneseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;

    public AnamneseService(AnamneseRepository anamneseRepository) {
        this.repository = anamneseRepository;
    }

    @Transactional
    public Anamnese save(Anamnese anamnese){
        System.out.println("=== SERVICE: Salvando anamnese ===");
        System.out.println("Peso: " + anamnese.getPeso());
        System.out.println("Altura: " + anamnese.getAltura());
        System.out.println("Idade: " + anamnese.getIdade());
        System.out.println("Sexo: " + anamnese.getSexo());
        System.out.println("Objective: " + anamnese.getObjective());
        System.out.println("Alergias: " + anamnese.getFoodAllergy());
        System.out.println("Condições: " + anamnese.getHealthCondition());
        System.out.println("Dietas: " + anamnese.getDiet());
        Anamnese saved = repository.save(anamnese);
        System.out.println("=== SERVICE: Salvo com sucesso! ID: " + saved.getAnamneseID() + " ===");
        return saved;

    }

    public AnamneseResponseDTO getAnamneseById(UUID id){
        Anamnese anamnese = repository.findByAnamneseID(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return new AnamneseResponseDTO(anamnese);
    }
}
