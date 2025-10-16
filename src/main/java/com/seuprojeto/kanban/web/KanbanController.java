package com.seuprojeto.kanban.web;

import com.seuprojeto.kanban.domain.StatusProjeto;
import com.seuprojeto.kanban.dto.ApiError;
import com.seuprojeto.kanban.dto.ProjetoDTO;
import com.seuprojeto.kanban.mapper.ProjetoMapper;
import com.seuprojeto.kanban.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/kanban")
@Tag(name = "Kanban", description = "Visão do board por colunas (status) e transições controladas entre estados.")
@SecurityRequirement(name = "bearerAuth")
public class KanbanController {

  private final ProjetoService service;
  private final ProjetoMapper mapper;

  public KanbanController(ProjetoService service, ProjetoMapper mapper){
    this.service = service; this.mapper = mapper;
  }

  @Operation(summary = "Board do Kanban", description = "Retorna um mapa `StatusProjeto -> Lista de Projetos` para montar o board com colunas A_INICIAR, EM_ANDAMENTO, ATRASADO e CONCLUIDO.")
  @ApiResponse(responseCode = "200", description = "Board retornado",
      content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name="ok", value = "{\n  \"A_INICIAR\": [],\n  \"EM_ANDAMENTO\": [],\n  \"ATRASADO\": [],\n  \"CONCLUIDO\": []\n}")))
  @GetMapping
  public ResponseEntity<Map<StatusProjeto, List<ProjetoDTO>>> board(){
    Map<StatusProjeto, List<ProjetoDTO>> resp = new EnumMap<>(StatusProjeto.class);
    for (var s : StatusProjeto.values()){
      var list = service.listar(null, s, org.springframework.data.domain.PageRequest.of(0, 100)).map(mapper::toDTO).getContent();
      resp.put(s, list);
    }
    return ResponseEntity.ok(resp);
  }

  public record TransicaoRequest(StatusProjeto para){}

  @Operation(summary = "Transição de status do card", description = "Tenta mover o projeto para o status **para**. O serviço ajusta datas automaticamente (início/término) e valida as regras.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Transição aplicada", content = @Content(schema = @Schema(implementation = ProjetoDTO.class))),
    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada", content = @Content(schema = @Schema(implementation = ApiError.class),
      examples = @ExampleObject(value = "{\n  \"message\": \"Transição inválida: após ajustes, projeto ficou em A_INICIAR.\",\n  \"status\": 422\n}")))
  })
  @PostMapping("/{id}/transicao")
  public ResponseEntity<ProjetoDTO> transicao(
      @Parameter(description = "ID do projeto") @PathVariable UUID id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Status de destino",
          required = true, content = @Content(examples = @ExampleObject(value="{ \"para\": \"EM_ANDAMENTO\" }"))) @RequestBody TransicaoRequest req){
    var p = service.transicionar(id, req.para());
    return ResponseEntity.ok(mapper.toDTO(p));
  }
}
