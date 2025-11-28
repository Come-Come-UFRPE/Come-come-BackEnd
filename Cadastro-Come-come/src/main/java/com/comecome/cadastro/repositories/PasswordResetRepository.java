package com.comecome.cadastro.repositories;

import com.comecome.cadastro.models.VerificationToken;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.models.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByUserAndTokenType(User user, TokenType type);

}
