package com.tpmotors.dto;

import com.tpmotors.model.TipoTransacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PagamentoRequest {

    @NotNull(message = "O ID do carro é obrigatório")
    private Long carroId;

    private TipoTransacao tipo = TipoTransacao.COMPRA_DIRETA;

    @NotNull(message = "O valor total é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    private BigDecimal valorTotal;

    private BigDecimal valorEntrada;
    private Integer numeroParcelas = 1;

    public PagamentoRequest() {
    }

    public Long getCarroId() {
        return carroId;
    }

    public void setCarroId(Long carroId) {
        this.carroId = carroId;
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
}
