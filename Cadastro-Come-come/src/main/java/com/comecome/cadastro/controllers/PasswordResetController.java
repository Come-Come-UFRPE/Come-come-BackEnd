package com.comecome.cadastro.controllers;

import com.comecome.cadastro.dtos.EmailRequestDTO;
import com.comecome.cadastro.dtos.TokenDTO;
import com.comecome.cadastro.dtos.TokenPatchDTO;
import com.comecome.cadastro.services.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reset-password")
public class PasswordResetController {

    private final PasswordResetService passwordService;

    public PasswordResetController(PasswordResetService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/generate-token")
    public ResponseEntity<TokenDTO> generateToken(@RequestBody EmailRequestDTO emailRequest){
        String token = passwordService.createNewToken(emailRequest.email());
        TokenDTO dto = new TokenDTO(emailRequest.email(), token);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Boolean> validateToken(@RequestBody TokenDTO token){
        return ResponseEntity.ok(passwordService.validateToken(token.email(), token.token()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> resetPassword(@RequestBody TokenPatchDTO token){
        passwordService.resetPassword(token.email(),token.token(), token.newPassword());
        return ResponseEntity.ok().build();
    }

}
