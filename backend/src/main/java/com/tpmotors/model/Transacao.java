package com.tpmotors.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_transacao", nullable = false, unique = true, length = 64)
    private String codigoTransacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carro_id", nullable = false)
    private Carro carro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTransacao tipo = TipoTransacao.COMPRA_DIRETA;

    @NotNull(message = "O valor total é obrigatório")
    @Positive(message = "O valor total deve ser positivo")
    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_entrada", precision = 14, scale = 2)
    private BigDecimal valorEntrada = BigDecimal.ZERO;

    @Column(name = "numero_parcelas")
    private Integer numeroParcelas = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTransacao status = StatusTransacao.PENDENTE;

    @Column(name = "id_transacao_pagseguro", length = 100)
    private String idTransacaoPagSeguro;

    @Column(name = "link_pagamento", columnDefinition = "TEXT")
    private String linkPagamento;

    @Column(name = "qrcode_pix", columnDefinition = "TEXT")
    private String qrcodePix;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Transacao() {
    }

    public Transacao(String codigoTransacao, Carro carro, Usuario comprador, Usuario vendedor,
                     TipoTransacao tipo, BigDecimal valorTotal, BigDecimal valorEntrada, Integer numeroParcelas) {
        this.codigoTransacao = codigoTransacao;
        this.carro = carro;
        this.comprador = comprador;
        this.vendedor = vendedor;
        this.tipo = tipo != null ? tipo : TipoTransacao.COMPRA_DIRETA;
        this.valorTotal = valorTotal;
        this.valorEntrada = valorEntrada != null ? valorEntrada : BigDecimal.ZERO;
        this.numeroParcelas = numeroParcelas != null ? numeroParcelas : 1;
        this.status = StatusTransacao.PENDENTE;
    }

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }

    public void setCodigoTransacao(String codigoTransacao) {
        this.codigoTransacao = codigoTransacao;
    }

    public Carro getCarro() {
        return carro;
    }

    public void setCarro(Carro carro) {
        this.carro = carro;
    }

    public Usuario getComprador() {
        return comprador;
    }

    public void setComprador(Usuario comprador) {
        this.comprador = comprador;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorEntrada() {
        return valorEntrada;
    }

    public void setValorEntrada(BigDecimal valorEntrada) {
        this.valorEntrada = valorEntrada;
    }

    public Integer getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(Integer numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public StatusTransacao getStatus() {
        return status;
    }

    public void setStatus(StatusTransacao status) {
        this.status = status;
    }

    public String getIdTransacaoPagSeguro() {
        return idTransacaoPagSeguro;
    }

    public void setIdTransacaoPagSeguro(String idTransacaoPagSeguro) {
        this.idTransacaoPagSeguro = idTransacaoPagSeguro;
    }

    public String getLinkPagamento() {
        return linkPagamento;
    }

    public void setLinkPagamento(String linkPagamento) {
        this.linkPagamento = linkPagamento;
    }

    public String getQrcodePix() {
        return qrcodePix;
    }

    public void setQrcodePix(String qrcodePix) {
        this.qrcodePix = qrcodePix;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
