package com.comecome.anamnese.controllers;

import com.comecome.anamnese.dtos.AnamnesePatchRecordDTO;
import com.comecome.anamnese.dtos.AnamneseRecordDTO;
import com.comecome.anamnese.dtos.AnamneseResponseDTO;
import com.comecome.anamnese.models.Anamnese;
import com.comecome.anamnese.models.enums.*;
import com.comecome.anamnese.services.AnamneseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnamneseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnamneseService anamneseService;

    @InjectMocks
    private AnamneseController anamneseController;

    private ObjectMapper objectMapper;
    private UUID sampleUserId;
    private Anamnese sampleAnamnese;
    private AnamneseResponseDTO sampleResponseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(anamneseController).build();
        objectMapper = new ObjectMapper();

        sampleUserId = UUID.randomUUID();

        sampleAnamnese = new Anamnese();
        sampleAnamnese.setAnamneseID(UUID.randomUUID());
        sampleAnamnese.setUserID(sampleUserId);
        sampleAnamnese.setPeso(70.0);
        sampleAnamnese.setAltura(170);
        sampleAnamnese.setIdade(24);
        sampleAnamnese.setSexo(Gender.FEMININO);
        sampleAnamnese.setObjective(Objective.HABITOS_SAUDAVEIS);
        sampleAnamnese.setFoodAllergy(new HashSet<>());
        sampleAnamnese.setHealthCondition(new HashSet<>());
        sampleAnamnese.setDiet(new HashSet<>());

        sampleResponseDTO = AnamneseResponseDTO.fromEntity(sampleAnamnese);
    }

    @Test
    @DisplayName("GET /api/anamnese/{userID} - Deve retornar 200 OK com os dados da anamnese")
    void testGetAnamnese_ReturnsOk() throws Exception {
        when(anamneseService.getAnamneseById(sampleUserId)).thenReturn(sampleResponseDTO);

        mockMvc.perform(get("/api/anamnese/{userID}", sampleUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userID").value(sampleUserId.toString()))
                .andExpect(jsonPath("$.peso").value(70.0));
    }

    @Test
    @DisplayName("POST /api/anamnese - Deve criar anamnese com retorno 201 Created")
    void testCreateAnamnese_ReturnsCreated() throws Exception {
        AnamneseRecordDTO dto = new AnamneseRecordDTO(
                sampleUserId,
                70.0,
                170,
                24,
                Gender.FEMININO,
                Objective.HABITOS_SAUDAVEIS,
                Set.of(),
                Set.of(),
                Set.of()
        );

        when(anamneseService.save(any(Anamnese.class))).thenReturn(sampleResponseDTO);

        mockMvc.perform(post("/api/anamnese")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userID").value(sampleUserId.toString()));
    }

    @Test
    @DisplayName("PATCH /api/anamnese/{userID} - Deve modificar anamnese com retorno 200 OK")
    void testModifyUser_ReturnsOk() throws Exception {
        AnamnesePatchRecordDTO patchDto = new AnamnesePatchRecordDTO(
                72.0,
                170,
                25,
                Objective.GANHO_MASSA_MUSCULAR,
                Set.of(),
                Set.of(),
                Set.of()
        );

        when(anamneseService.partialUpdate(eq(sampleUserId), any(AnamnesePatchRecordDTO.class)))
                .thenReturn(sampleAnamnese);

        mockMvc.perform(patch("/api/anamnese/{userID}", sampleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userID").value(sampleUserId.toString()));
    }
}
