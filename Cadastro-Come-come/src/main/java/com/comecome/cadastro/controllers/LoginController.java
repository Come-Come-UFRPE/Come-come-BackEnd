package com.comecome.cadastro.controllers;

import com.comecome.cadastro.dtos.LoginDTO;
import com.comecome.cadastro.dtos.LoginResponseDTO;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import com.comecome.cadastro.services.LoginService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        return ResponseEntity.ok(loginService.verify(loginDTO.email(), loginDTO.password()));
    }

}
