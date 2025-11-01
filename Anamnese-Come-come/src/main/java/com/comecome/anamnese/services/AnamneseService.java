package com.comecome.anamnese.services;

import com.comecome.anamnese.dtos.AnamnesePatchRecordDTO;
import com.comecome.anamnese.dtos.AnamneseResponseDTO;
import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.repositories.AnamneseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;

    public AnamneseService(AnamneseRepository anamneseRepository) {
        this.repository = anamneseRepository;
    }

    @Transactional
    public AnamneseResponseDTO save(Anamnese anamnese){
        Anamnese savedEntity = repository.save(anamnese);
        return AnamneseResponseDTO.fromEntity(savedEntity);
    }

    public AnamneseResponseDTO getAnamneseById(UUID id){
        Anamnese anamnese = repository.findByAnamneseID(id).orElseThrow(() -> new RuntimeException("Anamnese não encontrada"));
        return new AnamneseResponseDTO(anamnese);
    }

    @Transactional
    public Anamnese partialUpdate(UUID id, AnamnesePatchRecordDTO dto){
        Anamnese updateAnamnese = repository.findByAnamneseID(id)
                .orElseThrow(() -> new RuntimeException("Anamnese não encontrada"));

        if (dto.peso() != null){
            updateAnamnese.setPeso(dto.peso());
        }

        if (dto.altura() != null){
            updateAnamnese.setAltura(dto.altura());
        }

        if (dto.idade() != null){
            updateAnamnese.setIdade(dto.idade());
        }

        if (dto.objective() != null){
            updateAnamnese.setObjective(dto.objective());
        }

        // Remove e atualiza novamente (melhor?)
        if (dto.foodAllergy() != null) {
            updateAnamnese.getFoodAllergy().clear();
            updateAnamnese.getFoodAllergy().addAll(dto.foodAllergy());
        }

        if (dto.healthCondition() != null) {
            updateAnamnese.getHealthCondition().clear();
            updateAnamnese.getHealthCondition().addAll(dto.healthCondition());
        }

        if (dto.diet() != null) {
            updateAnamnese.getDiet().clear();
            updateAnamnese.getDiet().addAll(dto.diet());
        }


        return repository.save(updateAnamnese);
    }
}
