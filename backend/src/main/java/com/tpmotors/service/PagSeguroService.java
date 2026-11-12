package com.tpmotors.service;

import com.tpmotors.model.Transacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service Mockado do PagSeguro (PagBank).
 * Prepara a estrutura completa para transição futura com as chamadas REST oficiais
 * da API v4/v5 do PagSeguro (Checkout transparente, Pix e Cartão de Crédito).
 */
@Service
public class PagSeguroService {

    private static final Logger logger = LoggerFactory.getLogger(PagSeguroService.class);

    @Value("${tpmotors.pagseguro.token:TOKEN_MOCK}")
    private String token;

    @Value("${tpmotors.pagseguro.sandbox:true}")
    private boolean sandbox;

    @Value("${tpmotors.pagseguro.notification-url:http://localhost:8080/api/transacoes/webhook-pagseguro}")
    private String notificationUrl;

    public static class RespostaPagSeguro {
        private String idTransacaoPagSeguro;
        private String statusPagamento;
        private String linkPagamento;
        private String qrcodePix;
        private String mensagem;

        public RespostaPagSeguro(String idTransacaoPagSeguro, String statusPagamento, String linkPagamento, String qrcodePix, String mensagem) {
            this.idTransacaoPagSeguro = idTransacaoPagSeguro;
            this.statusPagamento = statusPagamento;
            this.linkPagamento = linkPagamento;
            this.qrcodePix = qrcodePix;
            this.mensagem = mensagem;
        }

        public String getIdTransacaoPagSeguro() {
            return idTransacaoPagSeguro;
        }

        public String getStatusPagamento() {
            return statusPagamento;
        }

        public String getLinkPagamento() {
            return linkPagamento;
        }

        public String getQrcodePix() {
            return qrcodePix;
        }

        public String getMensagem() {
            return mensagem;
        }
    }

    /**
     * Cria uma ordem/sessão de pagamento no PagSeguro (Mock).
     */
    public RespostaPagSeguro criarOrdemPagamento(Transacao transacao) {
        logger.info("[PagSeguro Mock] Iniciando checkout de transação TPMotors: {}", transacao.getCodigoTransacao());
        logger.info("[PagSeguro Mock] Ambiente Sandbox: {}, URL de Notificação: {}", sandbox, notificationUrl);

        // Gera IDs e referências simuladas do PagSeguro
        String idTransacaoPagSeguro = "PAGSEG_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String linkPagamento = "https://sandbox.pagseguro.uol.com.br/v2/checkout/payment.html?code=" + idTransacaoPagSeguro;
        String qrcodePix = "00020126580014br.gov.bcb.pix0136" + UUID.randomUUID() + "520400005303986540" + transacao.getValorTotal() + "5802BR5915TPMOTORS VEICULOS6009SAO PAULO62070503***6304";

        logger.info("[PagSeguro Mock] Ordem gerada com sucesso. ID PagSeguro: {}", idTransacaoPagSeguro);

        return new RespostaPagSeguro(
                idTransacaoPagSeguro,
                "WAITING_FOR_PAYMENT",
                linkPagamento,
                qrcodePix,
                "Checkout PagSeguro criado com sucesso (Ambiente Mock Sandbox)"
        );
    }

    /**
     * Processa notificações e webhooks enviados pelo PagSeguro.
     */
    public Map<String, Object> processarWebhook(String idNotificacao) {
        logger.info("[PagSeguro Mock] Webhook recebido para a notificação: {}", idNotificacao);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("notificationId", idNotificacao);
        resultado.put("status", "PAID");
        resultado.put("message", "Pagamento aprovado com sucesso via webhook mock");
        return resultado;
    }

    /**
     * Consulta status de pagamento direto no gateway (Mock).
     */
    public String consultarStatus(String idTransacaoPagSeguro) {
        logger.info("[PagSeguro Mock] Consultando status da transação: {}", idTransacaoPagSeguro);
        return "PAID";
    }
}
