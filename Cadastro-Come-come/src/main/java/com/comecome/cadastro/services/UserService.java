package com.comecome.cadastro.services;

import com.comecome.cadastro.dtos.UserPatchRecordDto;
import com.comecome.cadastro.dtos.UserResponseDTO;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public UserResponseDTO getUserById(UUID id){
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return new UserResponseDTO(user);
    }

    @Transactional
    public User save(User user){
        System.out.println(user.getEmail());
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email já cadastrado");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User partialUpdate(UUID id, UserPatchRecordDto dto) {

        // 1. Busca a tupla (entidade) original do banco de dados
        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        if (dto.name() != null) {
            userFromDb.setName(dto.name());
        }

        if (dto.cidade() != null) {
            // O usuário enviou um e-mail, então vamos atualizar
            userFromDb.setCidade(dto.cidade());
        }

        if (dto.estado() != null) {
            // O usuário enviou um estado, então vamos atualizar
            userFromDb.setEstado(dto.estado());
        }

        return userRepository.save(userFromDb);
    }
}
