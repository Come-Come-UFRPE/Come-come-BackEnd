package com.comecome.cadastro.services;

import com.comecome.cadastro.dtos.TokenDTO;
import com.comecome.cadastro.exceptions.TokenExpiredException;
import com.comecome.cadastro.exceptions.UserNotFoundException;
import com.comecome.cadastro.models.VerificationToken;
import com.comecome.cadastro.models.User;
import com.comecome.cadastro.models.enums.TokenType;
import com.comecome.cadastro.repositories.PasswordResetRepository;
import com.comecome.cadastro.repositories.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class TokenService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    private static final String fila = "send-emails";

    public TokenService(UserRepository userRepository, PasswordResetRepository repository, PasswordEncoder passwordEncoder, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordRepository = repository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
    }



    //Função relacionada com a criação de token
    @Transactional
    public void createNewToken(String email, TokenType tokenType){
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        String otpCode = generateOTPToken();

        TokenDTO sendDto = new TokenDTO(email,otpCode, tokenType.toString());

        persistNewToken(user, otpCode, tokenType);

        rabbitTemplate.convertAndSend("",fila,sendDto);

    }

    // Persistir um novo token no Banco de Dados
    public void persistNewToken(User user, String rawOtp, TokenType tokenType){
        passwordRepository.findByUserAndTokenType(user, tokenType).ifPresent(passwordRepository::delete);

        String hashing = passwordEncoder.encode(rawOtp);

        VerificationToken newToken = new VerificationToken(hashing, 10, tokenType, user);

        passwordRepository.save(newToken);
    }

    //Gerar token seguro
    public String generateOTPToken(){

        SecureRandom random = new SecureRandom();

        int otp = 100000 + random.nextInt(900000);

        return String.valueOf(otp);
    }

    //Função principal de Validação de Token
    @Transactional
    public boolean validateToken(String email, String receivedOtp, TokenType type) {
        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        Optional<VerificationToken> tokenOptional = passwordRepository.findByUserAndTokenType(user, type);
        if (tokenOptional.isEmpty()) {
            return false;
        }

        VerificationToken token = tokenOptional.get();

        // Validações de Segurança
        if (token.isExpired() || token.isMaxAttemptExceed()) {
            passwordRepository.delete(token);
            return false;
        }

        // Verifica se o código bate com o hash
        if (passwordEncoder.matches(receivedOtp, token.getTokenHash())) {
            return true;
        }

        token.setAttemptCount(token.getAttemptCount() + 1);
        passwordRepository.save(token);
        return false;

    }

    //Função que vai atuar no reset da senha
    public void resetPassword(String email, String otp, String newPassword){
        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        VerificationToken token = passwordRepository.findByUserAndTokenType(user, TokenType.PASSWORD_RESET).orElseThrow(() -> new TokenExpiredException("Token de usuário não encontrado!"));

        if (token.isExpired() || !passwordEncoder.matches(otp, token.getTokenHash())) {
            throw new TokenExpiredException("Token inválido ou expirado na etapa final!");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));

        userRepository.save(user);



        passwordRepository.delete(token);

    }

    //Função que vai atuar na confirmação de senha
    public void confirmEmail(String email, String otp){
        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        VerificationToken token = passwordRepository.findByUserAndTokenType(user, TokenType.EMAIL_VERIFICATION).orElseThrow(() -> new TokenExpiredException("Token de usuário não encontrado!"));

        if (token.isExpired() || !passwordEncoder.matches(otp, token.getTokenHash())) {
            throw new TokenExpiredException("Token inválido ou expirado na etapa final!");
        }

        user.setFezConfirmacao(true);
        userRepository.save(user);

        passwordRepository.delete(token);

    }

}
