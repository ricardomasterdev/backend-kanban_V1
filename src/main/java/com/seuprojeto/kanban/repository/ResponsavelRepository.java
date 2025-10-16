package com.seuprojeto.kanban.repository;

import com.seuprojeto.kanban.domain.Responsavel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsavelRepository extends JpaRepository<Responsavel, Long> {

    // paginação por secretaria
    Page<Responsavel> findBySecretaria_Id(Long secretariaId, Pageable pageable);

    // filtro por nome (contém), paginado
    Page<Responsavel> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // filtro por nome + secretaria, paginado
    Page<Responsavel> findByNomeContainingIgnoreCaseAndSecretaria_Id(
            String nome, Long secretariaId, Pageable pageable
    );

    // e-mail único (case-insensitive)
    boolean existsByEmailIgnoreCase(String email);
}
