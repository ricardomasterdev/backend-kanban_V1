package com.seuprojeto.kanban.dto;

import com.seuprojeto.kanban.domain.StatusProjeto;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;
import java.util.UUID;

public record ProjetoDTO(
        UUID id,
        String nome,
        StatusProjeto status,
        LocalDate inicioPrevisto,
        LocalDate terminoPrevisto,
        LocalDate inicioRealizado,
        LocalDate terminoRealizado,
        int diasAtraso,
        int percentualTempoRestante,
        // ===== Compat: campos antigos (mantidos) =====
        Set<Long> responsaveisIds,
        Long secretariaId,
        // ===== Novos campos solicitados =====
        SecretariaDTO secretaria,
        List<ResponsavelShortDTO> responsaveis
) {}
