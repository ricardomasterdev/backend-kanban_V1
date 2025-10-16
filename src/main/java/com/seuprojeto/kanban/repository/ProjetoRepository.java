package com.seuprojeto.kanban.repository;

import com.seuprojeto.kanban.domain.Projeto;
import com.seuprojeto.kanban.domain.StatusProjeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjetoRepository extends JpaRepository<Projeto, UUID> {

    @Override
    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findByStatus(StatusProjeto status, Pageable pageable);

    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findBySecretaria_Id(Long secretariaId, Pageable pageable);

    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findByNomeContainingIgnoreCaseAndStatus(String nome, StatusProjeto status, Pageable pageable);

    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findByNomeContainingIgnoreCaseAndSecretaria_Id(String nome, Long secretariaId, Pageable pageable);

    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findBySecretaria_IdAndStatus(Long secretariaId, StatusProjeto status, Pageable pageable);

    @EntityGraph(attributePaths = {"secretaria", "responsaveis"})
    Page<Projeto> findByNomeContainingIgnoreCaseAndSecretaria_IdAndStatus(
            String nome, Long secretariaId, StatusProjeto status, Pageable pageable
    );
}
