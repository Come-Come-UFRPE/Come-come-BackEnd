package com.comecome.cadastro.services;

import com.comecome.cadastro.models.PasswordResetToken;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.repositories.PasswordResetRepository;
import com.comecome.cadastro.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,PasswordResetRepository repository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }



    //Função relacionada com a criação de token
    @Transactional
    public String createNewToken(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String otpCode = generateOTPToken();

        persistNewToken(user, otpCode);

        return otpCode;
    }

    // Persistir um novo token no Banco de Dados
    public void persistNewToken(User user, String rawOtp){
        passwordRepository.findByUser(user).ifPresent(passwordRepository::delete);

        String hashing = passwordEncoder.encode(rawOtp);

        PasswordResetToken newToken = new PasswordResetToken(hashing, 10, user);

        passwordRepository.save(newToken);
    }

    //Gerar token seguro
    public String generateOTPToken(){

        SecureRandom random = new SecureRandom();

        int otp = 100000 + random.nextInt(900000);

        return String.valueOf(otp);
    }

    //Função principal de Validação de Token
    public boolean validateToken(String email, String receivedOtp) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Optional<PasswordResetToken> tokenOptional = passwordRepository.findByUser(user);
        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOptional.get();

        // Validações de Segurança
        if (token.isExpired() || token.isMaxAttemptExceed()) {
            passwordRepository.delete(token);
            return false;
        }

        // Verifica se o código bate com o hash
        if (passwordEncoder.matches(receivedOtp, token.getTokenHash())) {
            passwordRepository.delete(token);
            return true;
        }

        token.setAttemptCount(token.getAttemptCount() + 1);
        passwordRepository.save(token);
        return false;

    }
}
