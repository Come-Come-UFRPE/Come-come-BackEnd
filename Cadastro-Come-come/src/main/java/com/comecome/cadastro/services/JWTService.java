package com.comecome.cadastro.services;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.comecome.cadastro.exceptions.InvalidTokenException;
import com.comecome.cadastro.exceptions.TokenExpiredException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class JWTService {

    private final SecretKey secretKey;

    public JWTService() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            this.secretKey = keyGen.generateKey();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao inicializar o JWTService", e);
        }
    }

    // private Key getKey(){
    //     byte[] keyBytes = Base64.getDecoder().decode(secretKey);
    //     return Keys.hmacShaKeyFor(keyBytes);
    // }

    public String generateToken(String email) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000 * 60 * 60); // 1 hora em milissegundos

        return Jwts.builder()
                .claims(claims)                 
                .subject(email)                 
                .issuedAt(now)                  
                .expiration(expiration)         
                .signWith(secretKey)                  
                .compact();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()            // nova API (substitui parserBuilder)
                .verifyWith(secretKey)      // verifica assinatura com a chave
                .build()
                .parseSignedClaims(token)   // faz o parsing do token JWT
                .getPayload();              // retorna os "claims" (dados do token)
        } 
        catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException();  
        } 
        catch (io.jsonwebtoken.JwtException e) {
            throw new InvalidTokenException();
        }
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token) {
        
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        final boolean expired = extractAllClaims(token).getExpiration().before(new Date());
        return email.equals(userDetails.getUsername()) && !expired;
    }
}
