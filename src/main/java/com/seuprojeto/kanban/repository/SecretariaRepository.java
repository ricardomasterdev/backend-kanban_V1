// src/main/java/com/seuprojeto/kanban/repository/SecretariaRepository.java
package com.seuprojeto.kanban.repository;

import com.seuprojeto.kanban.domain.Secretaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecretariaRepository extends JpaRepository<Secretaria, Long> {

    Page<Secretaria> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
