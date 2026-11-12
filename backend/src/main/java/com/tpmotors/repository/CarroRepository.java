package com.tpmotors.repository;

import com.tpmotors.model.Carro;
import com.tpmotors.model.StatusCarro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarroRepository extends JpaRepository<Carro, Long> {
    List<Carro> findByStatus(StatusCarro status);
    List<Carro> findByProprietarioId(Long proprietarioId);
    List<Carro> findByMarcaContainingIgnoreCaseOrModeloContainingIgnoreCase(String marca, String modelo);
}
