package com.tpmotors.service;

import com.tpmotors.model.Role;
import com.tpmotors.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${tpmotors.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Value("${tpmotors.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            // Garante tamanho de chave mínimo de 256 bits para HMAC-SHA256
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera o token JWT com payload requerido:
     * - subject: email
     * - user: nome do usuário
     * - status: "admin" ou "user"
     * - role: ROLE_ADMIN / ROLE_USER
     * - userId: identificador numérico
     */
    public String gerarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        String statusFormatted = usuario.getRole() == Role.ADMIN ? "admin" : "user";
        
        claims.put("user", usuario.getNome());
        claims.put("status", statusFormatted);
        claims.put("role", usuario.getRole().name());
        claims.put("userId", usuario.getId());

        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getEmail())
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public String extrairNome(String token) {
        return (String) extrairClaims(token).get("user");
    }

    public String extrairStatus(String token) {
        return (String) extrairClaims(token).get("status");
    }

    public Claims extrairClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
