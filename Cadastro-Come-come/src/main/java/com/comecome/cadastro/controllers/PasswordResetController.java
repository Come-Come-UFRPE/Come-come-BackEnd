package com.comecome.cadastro.controllers;

import com.comecome.cadastro.dtos.EmailRequestDTO;
import com.comecome.cadastro.dtos.TokenDTO;
import com.comecome.cadastro.dtos.TokenPatchDTO;
import com.comecome.cadastro.models.enums.TokenType;
import com.comecome.cadastro.services.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reset-password")
public class PasswordResetController {

    private final TokenService passwordService;

    public PasswordResetController(TokenService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/generate-token")
    public ResponseEntity<Void> generateToken(@RequestBody EmailRequestDTO emailRequest){
        passwordService.createNewToken(emailRequest.email(), TokenType.PASSWORD_RESET);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Boolean> validateToken(@RequestBody TokenDTO token){
        return ResponseEntity.ok(passwordService.validateToken(token.email(), token.token(), TokenType.PASSWORD_RESET));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> resetPassword(@RequestBody TokenPatchDTO token){
        passwordService.resetPassword(token.email(),token.token(), token.newPassword());
        return ResponseEntity.ok().build();
    }

}
