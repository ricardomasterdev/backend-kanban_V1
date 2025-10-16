package com.seuprojeto.kanban.dto;

public record ResponsavelDTO(
        Long id,
        String nome,
        String email,
        String cargo,
        Long secretariaId
) {}
