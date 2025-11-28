package com.comecome.cadastro.controllers;

import com.comecome.cadastro.dtos.EmailRequestDTO;
import com.comecome.cadastro.dtos.TokenDTO;
import com.comecome.cadastro.dtos.TokenPatchDTO;
import com.comecome.cadastro.models.enums.TokenType;
import com.comecome.cadastro.services.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/email-confirmation")
public class EmailConfirmationController {

    private final TokenService emailService;

    public EmailConfirmationController(TokenService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/generate-token")
    public ResponseEntity<Void> generateToken(@RequestBody EmailRequestDTO emailRequest){
        emailService.createNewToken(emailRequest.email(), TokenType.EMAIL_VERIFICATION);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/confirm-email")
    public ResponseEntity<Void> resetPassword(@RequestBody TokenPatchDTO token){
        emailService.confirmEmail(token.email(),token.token());
        return ResponseEntity.ok().build();
    }
}
