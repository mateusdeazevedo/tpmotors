package com.tpmotors.dto;

import com.tpmotors.model.StatusCarro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CarroDTO {

    private Long id;

    @NotBlank(message = "A marca é obrigatória")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotNull(message = "Ano de fabricação é obrigatório")
    private Integer anoFabricacao;

    @NotNull(message = "Ano do modelo é obrigatório")
    private Integer anoModelo;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser positivo")
    private BigDecimal preco;

    private Integer quilometragem = 0;
    private String cambio = "Manual";
    private String combustivel = "Flex";
    private String cor;
    private String descricao;
    private String imagemUrl;
    private StatusCarro status = StatusCarro.DISPONIVEL;
    private Long proprietarioId;
    private String proprietarioNome;

    public CarroDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnoFabricacao() {
        return anoFabricacao;
    }

    public void setAnoFabricacao(Integer anoFabricacao) {
        this.anoFabricacao = anoFabricacao;
    }

    public Integer getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(Integer quilometragem) {
        this.quilometragem = quilometragem;
    }

    public String getCambio() {
        return cambio;
    }

    public void setCambio(String cambio) {
        this.cambio = cambio;
    }

    public String getCombustivel() {
        return combustivel;
    }

    public void setCombustivel(String combustivel) {
        this.combustivel = combustivel;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public StatusCarro getStatus() {
        return status;
    }

    public void setStatus(StatusCarro status) {
        this.status = status;
    }

    public Long getProprietarioId() {
        return proprietarioId;
    }

    public void setProprietarioId(Long proprietarioId) {
        this.proprietarioId = proprietarioId;
    }

    public String getProprietarioNome() {
        return proprietarioNome;
    }

    public void setProprietarioNome(String proprietarioNome) {
        this.proprietarioNome = proprietarioNome;
    }
}
