package com.comecome.cadastro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.comecome.cadastro.config.filters.JWTFilter;
import com.comecome.cadastro.services.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JWTFilter jwtFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                    .csrf(customizer -> customizer.disable())                                                                   //? Desabilita a proteção CSRF (para POST, PUT E DELETE)
                    .authorizeHttpRequests(auth -> auth
                                                        .requestMatchers(HttpMethod.POST, "/users", "/login").permitAll()       //? Permite requisições POST para /users sem autenticação
                                                        .requestMatchers(HttpMethod.POST, "/users/register").permitAll()       //? Permite requisições POST para /users sem autenticação
                                                        .requestMatchers(                                                        // ? Permissão do Swagger
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**"
                                                        ).permitAll()
                                                        .requestMatchers(                                                        // ? Permissão do Swagger
                                                                "reset-password/generate-token",
                                                                "reset-password/verify-token",
                                                                "reset-password/change-password"
                                                        ).permitAll()
                                                        .anyRequest().authenticated())                                          //? Todas as requisições precisam estar autenticadas
                    .httpBasic(Customizer.withDefaults())                                                                       //? Habilita a autenticação HTTP Basic
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))               //? Configura a política de criação de sessão como STATELESS (sem estado)
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    //* Configura o autenticador com base no MyUserDetailsService
    public DaoAuthenticationProvider authenticationProvider(MyUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);                                 //? Cria o provedor com base no UserDetails trazido do banco
        provider.setPasswordEncoder(passwordEncoder);                                                                           //? Configura o codificador de senha pedido pelo provedor  
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
}
