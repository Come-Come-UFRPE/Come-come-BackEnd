package com.comecome.cadastro.services;

import com.comecome.cadastro.exceptions.UserNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.comecome.cadastro.repositories.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {

        var user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);         //? Busca o usuário pelo email no repositório

        return User.builder()                                                                                                       //? Monta o UserDetails com as informações do usuário
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER") 
                .build();
    }
    
}
