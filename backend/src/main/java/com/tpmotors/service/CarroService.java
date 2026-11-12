package com.tpmotors.service;

import com.tpmotors.dto.CarroDTO;
import com.tpmotors.exception.ResourceNotFoundException;
import com.tpmotors.model.Carro;
import com.tpmotors.model.Role;
import com.tpmotors.model.StatusCarro;
import com.tpmotors.model.Usuario;
import com.tpmotors.repository.CarroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarroService {

    private final CarroRepository carroRepository;
    private final UsuarioService usuarioService;

    public CarroService(CarroRepository carroRepository, UsuarioService usuarioService) {
        this.carroRepository = carroRepository;
        this.usuarioService = usuarioService;
    }

    public List<CarroDTO> listarDisponiveis() {
        return carroRepository.findByStatus(StatusCarro.DISPONIVEL)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<CarroDTO> listarTodos() {
        return carroRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<CarroDTO> listarPorProprietario(Long proprietarioId) {
        return carroRepository.findByProprietarioId(proprietarioId)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<CarroDTO> buscarPorTermo(String termo) {
        return carroRepository.findByMarcaContainingIgnoreCaseOrModeloContainingIgnoreCase(termo, termo)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public CarroDTO buscarPorId(Long id) {
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com o ID: " + id));
        return converterParaDTO(carro);
    }

    public Carro buscarEntidadePorId(Long id) {
        return carroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com o ID: " + id));
    }

    @Transactional
    public CarroDTO salvar(CarroDTO dto, String emailUsuarioLogado) {
        Usuario proprietario = usuarioService.buscarPorEmail(emailUsuarioLogado);

        Carro carro = new Carro();
        carro.setMarca(dto.getMarca());
        carro.setModelo(dto.getModelo());
        carro.setAnoFabricacao(dto.getAnoFabricacao());
        carro.setAnoModelo(dto.getAnoModelo());
        carro.setPreco(dto.getPreco());
        carro.setQuilometragem(dto.getQuilometragem() != null ? dto.getQuilometragem() : 0);
        carro.setCambio(dto.getCambio() != null ? dto.getCambio() : "Manual");
        carro.setCombustivel(dto.getCombustivel() != null ? dto.getCombustivel() : "Flex");
        carro.setCor(dto.getCor());
        carro.setDescricao(dto.getDescricao());
        carro.setImagemUrl(dto.getImagemUrl());
        carro.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusCarro.DISPONIVEL);
        carro.setProprietario(proprietario);

        Carro salvo = carroRepository.save(carro);
        return converterParaDTO(salvo);
    }

    @Transactional
    public CarroDTO atualizar(Long id, CarroDTO dto, String emailUsuarioLogado) {
        Carro carro = buscarEntidadePorId(id);
        Usuario usuario = usuarioService.buscarPorEmail(emailUsuarioLogado);

        // Apenas o dono ou um ADMIN podem editar
        if (!carro.getProprietario().getId().equals(usuario.getId()) && usuario.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para alterar os dados deste veículo.");
        }

        carro.setMarca(dto.getMarca());
        carro.setModelo(dto.getModelo());
        carro.setAnoFabricacao(dto.getAnoFabricacao());
        carro.setAnoModelo(dto.getAnoModelo());
        carro.setPreco(dto.getPreco());
        carro.setQuilometragem(dto.getQuilometragem());
        carro.setCambio(dto.getCambio());
        carro.setCombustivel(dto.getCombustivel());
        carro.setCor(dto.getCor());
        carro.setDescricao(dto.getDescricao());
        if (dto.getImagemUrl() != null && !dto.getImagemUrl().isBlank()) {
            carro.setImagemUrl(dto.getImagemUrl());
        }
        if (dto.getStatus() != null) {
            carro.setStatus(dto.getStatus());
        }

        Carro atualizado = carroRepository.save(carro);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void excluir(Long id, String emailUsuarioLogado) {
        Carro carro = buscarEntidadePorId(id);
        Usuario usuario = usuarioService.buscarPorEmail(emailUsuarioLogado);

        if (!carro.getProprietario().getId().equals(usuario.getId()) && usuario.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para excluir este anúncio.");
        }

        carroRepository.delete(carro);
    }

    public CarroDTO converterParaDTO(Carro carro) {
        CarroDTO dto = new CarroDTO();
        dto.setId(carro.getId());
        dto.setMarca(carro.getMarca());
        dto.setModelo(carro.getModelo());
        dto.setAnoFabricacao(carro.getAnoFabricacao());
        dto.setAnoModelo(carro.getAnoModelo());
        dto.setPreco(carro.getPreco());
        dto.setQuilometragem(carro.getQuilometragem());
        dto.setCambio(carro.getCambio());
        dto.setCombustivel(carro.getCombustivel());
        dto.setCor(carro.getCor());
        dto.setDescricao(carro.getDescricao());
        dto.setImagemUrl(carro.getImagemUrl());
        dto.setStatus(carro.getStatus());
        if (carro.getProprietario() != null) {
            dto.setProprietarioId(carro.getProprietario().getId());
            dto.setProprietarioNome(carro.getProprietario().getNome());
        }
        return dto;
    }
}
