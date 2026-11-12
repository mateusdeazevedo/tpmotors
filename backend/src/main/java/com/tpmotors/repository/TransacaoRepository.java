package com.tpmotors.repository;

import com.tpmotors.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    Optional<Transacao> findByCodigoTransacao(String codigoTransacao);
    Optional<Transacao> findByIdTransacaoPagSeguro(String idTransacaoPagSeguro);
    List<Transacao> findByCompradorId(Long compradorId);
    List<Transacao> findByVendedorId(Long vendedorId);
}
