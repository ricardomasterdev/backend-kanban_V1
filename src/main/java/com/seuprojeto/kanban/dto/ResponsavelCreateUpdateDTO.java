package com.seuprojeto.kanban.dto;

public record ResponsavelCreateUpdateDTO(
        String nome,
        String email,
        String cargo,
        Long secretariaId
) {}
