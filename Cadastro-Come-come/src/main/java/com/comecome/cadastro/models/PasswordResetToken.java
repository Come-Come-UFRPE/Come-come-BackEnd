package com.comecome.cadastro.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tb_password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String tokenHash;

    private LocalDateTime expiryDate;

    private int attemptCount = 0;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private User user;

    public PasswordResetToken(String tokenHash, int timeToExpireinMinutes, User user) {
        this.tokenHash = tokenHash;
        this.expiryDate = LocalDateTime.now().plusMinutes(timeToExpireinMinutes);
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
