package com.tpmotors.controller;

import com.tpmotors.dto.AuthRequest;
import com.tpmotors.dto.AuthResponse;
import com.tpmotors.dto.RegistroRequest;
import com.tpmotors.model.Usuario;
import com.tpmotors.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = usuarioService.autenticar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request) {
        AuthResponse response = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> perfilAtual(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        return ResponseEntity.ok(usuario);
    }
}
