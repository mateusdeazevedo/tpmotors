package com.tpmotors.service;

import com.tpmotors.dto.AuthRequest;
import com.tpmotors.dto.AuthResponse;
import com.tpmotors.dto.RegistroRequest;
import com.tpmotors.exception.ResourceNotFoundException;
import com.tpmotors.model.Role;
import com.tpmotors.model.Usuario;
import com.tpmotors.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("O e-mail informado já está cadastrado.");
        }

        if (request.getDocumento() != null && !request.getDocumento().isBlank()) {
            if (usuarioRepository.existsByDocumento(request.getDocumento())) {
                throw new IllegalArgumentException("O documento informado já está cadastrado.");
            }
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setTelefone(request.getTelefone());
        usuario.setDocumento(request.getDocumento());
        usuario.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        usuario.setAtivo(true);

        Usuario salvo = usuarioRepository.save(usuario);
        String token = jwtService.gerarToken(salvo);

        return new AuthResponse(token, salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getRole());
    }

    public AuthResponse autenticar(AuthRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha incorretos."));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("E-mail ou senha incorretos.");
        }

        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário inativo no sistema.");
        }

        String token = jwtService.gerarToken(usuario);
        return new AuthResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole());
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void alterarStatus(Long id, boolean ativo) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
    }
}
