package com.seuprojeto.kanban.dto;

import java.time.LocalDate;
import java.util.Set;

public record ProjetoCreateUpdateDTO(
        String nome,
        LocalDate inicioPrevisto,
        LocalDate terminoPrevisto,
        LocalDate inicioRealizado,
        LocalDate terminoRealizado,
        Set<Long> responsaveisIds,   // <- vários responsáveis
        Long secretariaId            // <- secretaria opcional
) {}
