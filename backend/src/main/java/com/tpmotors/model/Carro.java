package com.tpmotors.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carros")
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A marca é obrigatória")
    @Column(nullable = false, length = 60)
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    @Column(nullable = false, length = 80)
    private String modelo;

    @NotNull(message = "Ano de fabricação é obrigatório")
    @Column(name = "ano_fabricacao", nullable = false)
    private Integer anoFabricacao;

    @NotNull(message = "Ano do modelo é obrigatório")
    @Column(name = "ano_modelo", nullable = false)
    private Integer anoModelo;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer quilometragem = 0;

    @Column(length = 30)
    private String cambio = "Manual";

    @Column(length = 30)
    private String combustivel = "Flex";

    @Column(length = 40)
    private String cor;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url", columnDefinition = "TEXT")
    private String imagemUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusCarro status = StatusCarro.DISPONIVEL;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_proprietario_id", nullable = false)
    private Usuario proprietario;

    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public Carro() {
    }

    public Carro(String marca, String modelo, Integer anoFabricacao, Integer anoModelo, BigDecimal preco,
                 Integer quilometragem, String cambio, String combustivel, String cor, String descricao,
                 String imagemUrl, Usuario proprietario) {
        this.marca = marca;
        this.modelo = modelo;
        this.anoFabricacao = anoFabricacao;
        this.anoModelo = anoModelo;
        this.preco = preco;
        this.quilometragem = quilometragem;
        this.cambio = cambio;
        this.combustivel = combustivel;
        this.cor = cor;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
        this.proprietario = proprietario;
        this.status = StatusCarro.DISPONIVEL;
    }

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
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

    public Usuario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Usuario proprietario) {
        this.proprietario = proprietario;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}
