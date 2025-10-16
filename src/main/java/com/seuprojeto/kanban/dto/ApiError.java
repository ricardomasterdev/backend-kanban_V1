package com.seuprojeto.kanban.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Modelo padronizado de erro")
public record ApiError(
  @Schema(description = "Mensagem de erro resumida", example = "Erro de validação") String message,
  @Schema(description = "Cód. HTTP", example = "400") int status,
  @Schema(description = "Data/hora do erro em UTC", example = "2025-10-15T12:34:56Z") Instant timestamp,
  @Schema(description = "Caminho da requisição", example = "/api/v1/projetos") String path,
  @Schema(description = "Detalhes por campo (quando houver)") Map<String, String> fields
) {}
