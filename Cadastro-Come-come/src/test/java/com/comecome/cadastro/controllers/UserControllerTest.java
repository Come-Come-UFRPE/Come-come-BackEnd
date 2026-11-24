package com.comecome.cadastro.controllers;

import com.comecome.cadastro.dtos.UserPatchRecordDto;
import com.comecome.cadastro.dtos.UserRecordDto;
import com.comecome.cadastro.dtos.UserResponseDTO;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.services.TokenService;
import com.comecome.cadastro.services.UserService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        sampleUserId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setUserId(sampleUserId);
        sampleUser.setName("Felipe Dev");
        sampleUser.setEmail("felipe@teste.com");
        sampleUser.setPassword("senha123");
        sampleUser.setCidade("Recife");
        sampleUser.setEstado("PE");
        sampleUser.setIdade(24);
    }

    @Test
    @DisplayName("GET /users - Deve listar usuários com status 200")
    void testGetUsers_ReturnsOk() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Felipe Dev"))
                .andExpect(jsonPath("$[0].email").value("felipe@teste.com"));
    }

    @Test
    @DisplayName("POST /users - Deve criar novo usuário com status 201")
    void testSaveUser_ReturnsCreated() throws Exception {
        UserRecordDto dto = new UserRecordDto("Felipe Dev", "felipe@teste.com", "senha123", "PE", "Recife", 24);
        when(userService.save(any(User.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Felipe Dev"));
    }

    @Test
    @DisplayName("PATCH /users/{id} - Deve atualizar parcialmente usuário com status 200")
    void testModifyUser_ReturnsOk() throws Exception {
        UserPatchRecordDto patchDto = new UserPatchRecordDto("Nome Atualizado", "Olinda", "PE", 25, false);
        sampleUser.setName("Nome Atualizado");
        sampleUser.setCidade("Olinda");

        when(userService.partialUpdate(eq(sampleUserId), any(UserPatchRecordDto.class))).thenReturn(sampleUser);

        mockMvc.perform(patch("/users/{id}", sampleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"))
                .andExpect(jsonPath("$.cidade").value("Olinda"));
    }
}
