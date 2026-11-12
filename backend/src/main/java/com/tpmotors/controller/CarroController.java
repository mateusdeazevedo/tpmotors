package com.tpmotors.controller;

import com.tpmotors.dto.CarroDTO;
import com.tpmotors.service.CarroService;
import com.tpmotors.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carros")
public class CarroController {

    private final CarroService carroService;
    private final UsuarioService usuarioService;

    public CarroController(CarroService carroService, UsuarioService usuarioService) {
        this.carroService = carroService;
        this.usuarioService = usuarioService;
    }

    /**
     * Vitrine pública: lista carros disponíveis ou filtra por termo.
     */
    @GetMapping
    public ResponseEntity<List<CarroDTO>> listarDisponiveis(@RequestParam(required = false) String busca) {
        if (busca != null && !busca.trim().isEmpty()) {
            return ResponseEntity.ok(carroService.buscarPorTermo(busca.trim()));
        }
        return ResponseEntity.ok(carroService.listarDisponiveis());
    }

    /**
     * Lista todos os carros (incluindo vendidos e em negociação - para painel admin).
     */
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CarroDTO>> listarTodos() {
        return ResponseEntity.ok(carroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarroDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(carroService.buscarPorId(id));
    }

    /**
     * Lista veículos anunciados pelo usuário logado.
     */
    @GetMapping("/meus-carros")
    public ResponseEntity<List<CarroDTO>> meusCarros(Authentication authentication) {
        var usuario = usuarioService.buscarPorEmail(authentication.getName());
        return ResponseEntity.ok(carroService.listarPorProprietario(usuario.getId()));
    }

    /**
     * Cadastra um novo carro na plataforma.
     */
    @PostMapping
    public ResponseEntity<CarroDTO> cadastrar(@Valid @RequestBody CarroDTO dto, Authentication authentication) {
        CarroDTO criado = carroService.salvar(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza dados de um veículo.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CarroDTO> atualizar(@PathVariable Long id,
                                              @Valid @RequestBody CarroDTO dto,
                                              Authentication authentication) {
        CarroDTO atualizado = carroService.atualizar(id, dto, authentication.getName());
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Exclui o anúncio do veículo.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
        carroService.excluir(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
