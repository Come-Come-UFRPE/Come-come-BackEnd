package com.comecome.anamnese.services;

import com.comecome.anamnese.dtos.AnamnesePatchRecordDTO;
import com.comecome.anamnese.dtos.AnamneseResponseDTO;
import com.comecome.anamnese.exceptions.AnamneseAlreadyRegisteredException;
import com.comecome.anamnese.exceptions.AnamneseNotFoundException;
import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.models.enums.*;
import com.comecome.anamnese.repositories.AnamneseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnamneseServiceTest {

    @Mock
    private AnamneseRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AnamneseService anamneseService;

    private Anamnese sampleAnamnese;
    private UUID sampleUserId;
    private UUID sampleAnamneseId;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();

        sampleUserId = UUID.randomUUID();
        sampleAnamneseId = UUID.randomUUID();

        sampleAnamnese = new Anamnese();
        sampleAnamnese.setAnamneseID(sampleAnamneseId);
        sampleAnamnese.setUserID(sampleUserId);
        sampleAnamnese.setPeso(72.5);
        sampleAnamnese.setAltura(175);
        sampleAnamnese.setIdade(25);
        sampleAnamnese.setSexo(Gender.MASCULINO);
        sampleAnamnese.setObjective(Objective.GANHO_MASSA_MUSCULAR);
        sampleAnamnese.setFoodAllergy(new HashSet<>(Set.of(FoodAllergy.LACTOSE)));
        sampleAnamnese.setHealthCondition(new HashSet<>());
        sampleAnamnese.setDiet(new HashSet<>(Set.of(Diet.BAIXO_CARBOIDRATO)));
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("Deve salvar uma nova anamnese com sucesso")
    void testSaveAnamnese_Success() {
        when(repository.findByUserID(sampleUserId)).thenReturn(Optional.empty());
        when(repository.save(any(Anamnese.class))).thenReturn(sampleAnamnese);

        AnamneseResponseDTO result = anamneseService.save(sampleAnamnese);

        assertNotNull(result);
        assertEquals(sampleUserId, result.userID());
        assertEquals(72.5, result.peso());
        verify(repository, times(1)).save(sampleAnamnese);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar anamnese para usuário já cadastrado")
    void testSaveAnamnese_AlreadyRegistered() {
        when(repository.findByUserID(sampleUserId)).thenReturn(Optional.of(sampleAnamnese));

        assertThrows(AnamneseAlreadyRegisteredException.class, () -> anamneseService.save(sampleAnamnese));
        verify(repository, never()).save(any(Anamnese.class));
    }

    @Test
    @DisplayName("Deve buscar anamnese por User ID com sucesso")
    void testGetAnamneseById_Success() {
        when(repository.findByUserID(sampleUserId)).thenReturn(Optional.of(sampleAnamnese));

        AnamneseResponseDTO result = anamneseService.getAnamneseById(sampleUserId);

        assertNotNull(result);
        assertEquals(sampleUserId, result.userID());
        assertEquals(175, result.altura());
    }

    @Test
    @DisplayName("Deve lançar AnamneseNotFoundException quando anamnese não for encontrada")
    void testGetAnamneseById_NotFound() {
        when(repository.findByUserID(sampleUserId)).thenReturn(Optional.empty());

        assertThrows(AnamneseNotFoundException.class, () -> anamneseService.getAnamneseById(sampleUserId));
    }

    @Test
    @DisplayName("Deve atualizar parcialmente a anamnese com sucesso")
    void testPartialUpdate_Success() {
        when(repository.findByUserID(sampleUserId)).thenReturn(Optional.of(sampleAnamnese));
        when(repository.save(any(Anamnese.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnamnesePatchRecordDTO patchDto = new AnamnesePatchRecordDTO(
                75.0,
                176,
                26,
                Objective.PERDA_CONTROLE_PESO,
                Set.of(FoodAllergy.AMENDOIM_E_OLEAGINOSAS),
                Set.of(),
                Set.of(Diet.VEGETARIANA)
        );

        Anamnese updated = anamneseService.partialUpdate(sampleUserId, patchDto);

        assertEquals(75.0, updated.getPeso());
        assertEquals(176, updated.getAltura());
        assertEquals(26, updated.getIdade());
        assertEquals(Objective.PERDA_CONTROLE_PESO, updated.getObjective());
        assertTrue(updated.getFoodAllergy().contains(FoodAllergy.AMENDOIM_E_OLEAGINOSAS));
        verify(repository).save(sampleAnamnese);
    }
}
