package com.comecome.cadastro.services;

import com.comecome.cadastro.dtos.UserPatchRecordDto;
import com.comecome.cadastro.dtos.UserResponseDTO;
import com.comecome.cadastro.exceptions.EmailAlreadyExistsException;
import com.comecome.cadastro.exceptions.UserNotFoundException;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.models.enums.TokenType;
import com.comecome.cadastro.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlobStorageService blobStorageService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setUserId(sampleUserId);
        sampleUser.setName("Felipe Dev");
        sampleUser.setEmail("felipe@teste.com");
        sampleUser.setPassword("senha123");
        sampleUser.setCidade("Recife");
        sampleUser.setEstado("PE");
        sampleUser.setIdade(24);
        sampleUser.setFezAnamnese(false);
        sampleUser.setFezConfirmacao(false);
    }

    @Test
    @DisplayName("Deve salvar novo usuário com senha criptografada e gerar token de verificação")
    void testSaveUser_Success() {
        when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.save(sampleUser);

        assertNotNull(savedUser);
        assertNotEquals("senha123", savedUser.getPassword(), "A senha deve ser criptografada com BCrypt");
        verify(userRepository, times(1)).save(sampleUser);
        verify(tokenService, times(1)).createNewToken(eq(sampleUser.getEmail()), eq(TokenType.EMAIL_VERIFICATION));
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistsException quando email já existir")
    void testSaveUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.save(sampleUser));
        verify(userRepository, never()).save(any(User.class));
        verify(tokenService, never()).createNewToken(anyString(), any());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void testGetUserById_Success() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        UserResponseDTO response = userService.getUserById(sampleUserId);

        assertNotNull(response);
        assertEquals(sampleUser.getName(), response.name());
        assertEquals(sampleUser.getEmail(), response.email());
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando ID de usuário não existir")
    void testGetUserById_NotFound() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(sampleUserId));
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void testGetUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<User> users = userService.getUsers();

        assertEquals(1, users.size());
        assertEquals(sampleUser.getName(), users.get(0).getName());
    }

    @Test
    @DisplayName("Deve atualizar parcialmente os dados de um usuário")
    void testPartialUpdate_Success() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPatchRecordDto patchDto = new UserPatchRecordDto("Novo Nome", "Olinda", "PE", 25, true);
        User updated = userService.partialUpdate(sampleUserId, patchDto);

        assertEquals("Novo Nome", updated.getName());
        assertEquals("Olinda", updated.getCidade());
        assertTrue(updated.isFezAnamnese());
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Deve atualizar status de anamnese com sucesso")
    void testAtualizarStatusAnamnese_Success() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.atualizarStatusAnamnese(sampleUserId);

        assertTrue(sampleUser.isFezAnamnese());
        verify(userRepository).save(sampleUser);
    }
}
