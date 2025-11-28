package com.comecome.cadastro.models;

import com.comecome.cadastro.models.enums.TokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tb_verification_token")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String tokenHash;

    private LocalDateTime expiryDate;


    private TokenType tokenType;

    private int attemptCount = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public VerificationToken(String tokenHash, int timeToExpireinMinutes, TokenType tokenType, User user) {
        this.tokenHash = tokenHash;
        this.expiryDate = LocalDateTime.now().plusMinutes(timeToExpireinMinutes);
        this.tokenType = tokenType;
        this.attemptCount = 0;
        this.user = user;
    }

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public boolean isMaxAttemptExceed(){
        return attemptCount >= 5;
    }
}
