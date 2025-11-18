package com.comecome.cadastro.repositories;

import com.comecome.cadastro.models.PasswordResetToken;
import com.comecome.cadastro.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByUser(User user);
}
