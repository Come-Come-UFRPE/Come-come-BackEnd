package com.comecome.cadastro.services;

import com.comecome.cadastro.exceptions.EmailAlreadyExistsException;
import com.comecome.cadastro.exceptions.UserNotFoundException;
import com.comecome.cadastro.repositories.UserRepository;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.comecome.cadastro.dtos.LoginResponseDTO;
import com.comecome.cadastro.models.User;

@Service
public class LoginService {
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserRepository userRepository;
    
    public LoginResponseDTO verify(String email, String senha){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));

        if(authentication.isAuthenticated()){
            return new LoginResponseDTO(jwtService.generateToken(email), findByEmail(email).getUserId().toString(), findByEmail(email).isFezAnamnese());
        }
        throw new EmailAlreadyExistsException();
    }

    public User findByEmail(String email){
        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return user;
    }
}
