package com.seuprojeto.kanban.service;

import com.seuprojeto.kanban.domain.Projeto;
import com.seuprojeto.kanban.domain.StatusProjeto;
import com.seuprojeto.kanban.dto.ProjetoCreateUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjetoService {

    Projeto criar(ProjetoCreateUpdateDTO dto);

    Projeto atualizar(UUID id, ProjetoCreateUpdateDTO dto);

    // ANTIGA (usada pelo Kanban) - manter!
    Page<Projeto> listar(String texto, StatusProjeto status, Pageable pageable);

    // NOVA (usada pelo /projetos com secretariaId vindo do front)
    Page<Projeto> listar(String texto, StatusProjeto status, Long secretariaId, Pageable pageable);

    Projeto obter(UUID id);

    void excluir(UUID id);

    Projeto transicionar(UUID id, StatusProjeto para);
}
