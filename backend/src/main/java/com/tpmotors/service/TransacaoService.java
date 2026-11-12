package com.tpmotors.service;

import com.tpmotors.dto.PagamentoRequest;
import com.tpmotors.exception.ResourceNotFoundException;
import com.tpmotors.model.*;
import com.tpmotors.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final CarroService carroService;
    private final UsuarioService usuarioService;
    private final PagSeguroService pagSeguroService;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            CarroService carroService,
                            UsuarioService usuarioService,
                            PagSeguroService pagSeguroService) {
        this.transacaoRepository = transacaoRepository;
        this.carroService = carroService;
        this.usuarioService = usuarioService;
        this.pagSeguroService = pagSeguroService;
    }

    @Transactional
    public Transacao iniciarNegociacaoEPagamento(PagamentoRequest request, String emailComprador) {
        Usuario comprador = usuarioService.buscarPorEmail(emailComprador);
        Carro carro = carroService.buscarEntidadePorId(request.getCarroId());

        if (carro.getProprietario().getId().equals(comprador.getId())) {
            throw new IllegalArgumentException("Você não pode comprar ou financiar o seu próprio veículo.");
        }

        if (carro.getStatus() == StatusCarro.VENDIDO) {
            throw new IllegalArgumentException("Este veículo já foi vendido.");
        }

        String codigoTransacao = "TPM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transacao transacao = new Transacao();
        transacao.setCodigoTransacao(codigoTransacao);
        transacao.setCarro(carro);
        transacao.setComprador(comprador);
        transacao.setVendedor(carro.getProprietario());
        transacao.setTipo(request.getTipo() != null ? request.getTipo() : TipoTransacao.COMPRA_DIRETA);
        transacao.setValorTotal(request.getValorTotal());
        transacao.setValorEntrada(request.getValorEntrada());
        transacao.setNumeroParcelas(request.getNumeroParcelas());
        transacao.setStatus(StatusTransacao.PENDENTE);

        // Chamada à integração do PagSeguro (Mock)
        PagSeguroService.RespostaPagSeguro respostaPagSeguro = pagSeguroService.criarOrdemPagamento(transacao);
        transacao.setIdTransacaoPagSeguro(respostaPagSeguro.getIdTransacaoPagSeguro());
        transacao.setLinkPagamento(respostaPagSeguro.getLinkPagamento());
        transacao.setQrcodePix(respostaPagSeguro.getQrcodePix());

        // Coloca o carro como EM_NEGOCIACAO temporariamente
        carro.setStatus(StatusCarro.EM_NEGOCIACAO);

        return transacaoRepository.save(transacao);
    }

    public List<Transacao> listarPorUsuario(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        if (usuario.getRole() == Role.ADMIN) {
            return transacaoRepository.findAll();
        }
        List<Transacao> comoComprador = transacaoRepository.findByCompradorId(usuario.getId());
        List<Transacao> comoVendedor = transacaoRepository.findByVendedorId(usuario.getId());
        comoComprador.addAll(comoVendedor);
        return comoComprador;
    }

    public Transacao buscarPorCodigo(String codigo) {
        return transacaoRepository.findByCodigoTransacao(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada: " + codigo));
    }

    @Transactional
    public Transacao atualizarStatusPagSeguro(String idTransacaoPagSeguro, StatusTransacao novoStatus) {
        Transacao transacao = transacaoRepository.findByIdTransacaoPagSeguro(idTransacaoPagSeguro)
                .orElseThrow(() -> new ResourceNotFoundException("Transação PagSeguro não localizada: " + idTransacaoPagSeguro));

        transacao.setStatus(novoStatus);
        if (novoStatus == StatusTransacao.APROVADA) {
            transacao.getCarro().setStatus(StatusCarro.VENDIDO);
        } else if (novoStatus == StatusTransacao.CANCELADA) {
            transacao.getCarro().setStatus(StatusCarro.DISPONIVEL);
        }

        return transacaoRepository.save(transacao);
    }
}
