package com.comecome.cadastro.services;

import com.comecome.cadastro.dtos.AnamneseDTO;
import com.comecome.cadastro.dtos.UserPatchRecordDto;
import com.comecome.cadastro.dtos.UserRecordDto;
import com.comecome.cadastro.dtos.UserResponseDTO;
import com.comecome.cadastro.exceptions.EmailAlreadyExistsException;
import com.comecome.cadastro.exceptions.UserNotFoundException;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.repositories.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return new UserResponseDTO(user);
    }

    @Transactional
    public User save(User user){
        System.out.println(user.getEmail());
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException();
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));

        return userRepository.save(user);



    }

    @Transactional
    public User partialUpdate(UUID id, UserPatchRecordDto dto) {

        // 1. Busca a tupla (entidade) original do banco de dados
        User userFromDb = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        if (dto.name() != null) {
            userFromDb.setName(dto.name());
        }

        if (dto.cidade() != null) {
            // O usuário enviou uma cidade, então vamos atualizar
            userFromDb.setCidade(dto.cidade());
        }

        if (dto.estado() != null) {
            // O usuário enviou um estado, então vamos atualizar
            userFromDb.setEstado(dto.estado());
        }

        if(dto.fezAnamnese()){
            userFromDb.setFezAnamnese(dto.fezAnamnese());
        }

        return userRepository.save(userFromDb);
    }

    @Transactional
    public void atualizarStatusAnamnese(UUID userId){

            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Usuario ID:" + userId + "não encontrado"));

            user.setFezAnamnese(true);
            userRepository.save(user);



    }


    @Transactional
    public void removeUser(UUID userId) {
        User removed = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        userRepository.delete(removed);
    }
}

