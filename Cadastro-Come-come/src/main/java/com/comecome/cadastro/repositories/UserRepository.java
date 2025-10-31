package com.comecome.cadastro.repositories;

import com.comecome.cadastro.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email); //? Método alternativo de busca de usuário pelo email
    Optional<User> findByUserId(UUID userID);
}
