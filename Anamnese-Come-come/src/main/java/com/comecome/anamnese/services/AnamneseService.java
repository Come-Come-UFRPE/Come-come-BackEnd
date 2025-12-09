package com.comecome.anamnese.services;

import com.comecome.anamnese.dtos.AnamnesePatchRecordDTO;
import com.comecome.anamnese.dtos.AnamneseResponseDTO;
import com.comecome.anamnese.exceptions.AnamneseAlreadyRegisteredException;
import com.comecome.anamnese.exceptions.AnamneseNotFoundException;
import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.repositories.AnamneseRepository;
import com.comecome.anamnese.dtos.AnamneseDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;
    private static final String fila = "anamnese-criada";
    private final RabbitTemplate rabbitTemplate;

    public AnamneseService(AnamneseRepository anamneseRepository, RabbitTemplate rabbitTemplate) {
        this.repository = anamneseRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public AnamneseResponseDTO save(Anamnese anamnese){
            if (repository.findByUserID(anamnese.getUserID()).isPresent()) {
                throw new AnamneseAlreadyRegisteredException();
            }
            Anamnese savedEntity = repository.save(anamnese);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    AnamneseDTO evento = new AnamneseDTO(anamnese.getUserID());

                    rabbitTemplate.convertAndSend("",fila, evento );
                }
            });
            return AnamneseResponseDTO.fromEntity(savedEntity);

    }

    public AnamneseResponseDTO getAnamneseById(UUID id){
        Anamnese anamnese = repository.findByUserID(id).orElseThrow(AnamneseNotFoundException::new);
        return new AnamneseResponseDTO(anamnese);
    }

    @Transactional
    public Anamnese partialUpdate(UUID id, AnamnesePatchRecordDTO dto){
        Anamnese updateAnamnese = repository.findByUserID(id)
                .orElseThrow((AnamneseNotFoundException::new));

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
