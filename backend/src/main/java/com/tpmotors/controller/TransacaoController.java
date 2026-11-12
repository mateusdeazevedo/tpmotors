package com.tpmotors.controller;

import com.tpmotors.dto.PagamentoRequest;
import com.tpmotors.model.StatusTransacao;
import com.tpmotors.model.Transacao;
import com.tpmotors.service.PagSeguroService;
import com.tpmotors.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;
    private final PagSeguroService pagSeguroService;

    public TransacaoController(TransacaoService transacaoService, PagSeguroService pagSeguroService) {
        this.transacaoService = transacaoService;
        this.pagSeguroService = pagSeguroService;
    }

    /**
     * Inicia a transação de compra/financiamento com geração de checkout PagSeguro.
     */
    @PostMapping("/checkout")
    public ResponseEntity<Transacao> iniciarCheckout(@Valid @RequestBody PagamentoRequest request,
                                                     Authentication authentication) {
        Transacao transacao = transacaoService.iniciarNegociacaoEPagamento(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
    }

    /**
     * Lista histórico de transações do usuário atual (ou todas se for ADMIN).
     */
    @GetMapping("/minhas")
    public ResponseEntity<List<Transacao>> minhasTransacoes(Authentication authentication) {
        List<Transacao> transacoes = transacaoService.listarPorUsuario(authentication.getName());
        return ResponseEntity.ok(transacoes);
    }

    /**
     * Consulta detalhes de uma transação específica pelo código.
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Transacao> buscarPorCodigo(@PathVariable String codigo) {
        Transacao transacao = transacaoService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(transacao);
    }

    /**
     * Webhook mock do PagSeguro para notificações automáticas de pagamento.
     */
    @PostMapping("/webhook-pagseguro")
    public ResponseEntity<Map<String, Object>> webhookPagSeguro(@RequestParam(required = false) String notificationCode) {
        Map<String, Object> resposta = pagSeguroService.processarWebhook(
                notificationCode != null ? notificationCode : "NOTIF_MOCK_SAMPLE"
        );
        return ResponseEntity.ok(resposta);
    }

    /**
     * Atualização manual do status da transação (Apenas ADMIN).
     */
    @PatchMapping("/{idPagSeguro}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transacao> atualizarStatus(@PathVariable String idPagSeguro,
                                                    @RequestParam StatusTransacao status) {
        Transacao atualizada = transacaoService.atualizarStatusPagSeguro(idPagSeguro, status);
        return ResponseEntity.ok(atualizada);
    }
}
