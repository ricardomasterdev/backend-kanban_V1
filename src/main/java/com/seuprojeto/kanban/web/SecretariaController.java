// src/main/java/com/seuprojeto/kanban/web/SecretariaController.java
package com.seuprojeto.kanban.web;

import com.seuprojeto.kanban.dto.ResponsavelDTO;
import com.seuprojeto.kanban.dto.SecretariaDTO;
import com.seuprojeto.kanban.mapper.ResponsavelMapper;
import com.seuprojeto.kanban.mapper.SecretariaMapper;
import com.seuprojeto.kanban.repository.ResponsavelRepository;
import com.seuprojeto.kanban.repository.SecretariaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/secretarias")
@Tag(name = "Secretarias", description = "Cadastro simples de Secretarias, para vincular a projetos e responsáveis.")
@SecurityRequirement(name = "bearerAuth")
public class SecretariaController {

    private final SecretariaRepository repo;
    private final SecretariaMapper mapper;

    // >>> para unificar o endpoint de responsáveis por secretaria:
    private final ResponsavelRepository responsavelRepo;
    private final ResponsavelMapper responsavelMapper;

    public SecretariaController(SecretariaRepository repo,
                                SecretariaMapper mapper,
                                ResponsavelRepository responsavelRepo,
                                ResponsavelMapper responsavelMapper) {
        this.repo = repo;
        this.mapper = mapper;
        this.responsavelRepo = responsavelRepo;
        this.responsavelMapper = responsavelMapper;
    }

    /**
     * GET /secretarias
     * Suporta dois modos:
     * - Autocomplete: ?q=...&limit=10           -> retorna ARRAY de DTO
     * - Lista paginada: ?q=...&page=0&size=50   -> retorna Page<DTO>
     *
     * Se "limit" vier (e page/size não vierem), devolvemos array.
     * Caso contrário, devolvemos Page.
     */
    @Operation(summary = "Lista Secretarias (array ou página)",
            description = "Quando informado 'limit' (sem page/size), retorna array. Caso contrário, retorna página.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ){
        boolean wantsArray = (limit != null) && (page == null && size == null);

        if (wantsArray) {
            int lim = Math.max(1, Math.min(limit, 500));
            var pr = PageRequest.of(0, Math.min(lim, 500), Sort.by(Sort.Direction.ASC, "nome"));
            if (q != null && !q.isBlank()) {
                var result = repo.findByNomeContainingIgnoreCase(q.trim(), pr);
                List<SecretariaDTO> arr = result.stream().map(mapper::toDTO).toList();
                return ResponseEntity.ok(arr);
            } else {
                var result = repo.findAll(pr);
                List<SecretariaDTO> arr = result.stream().map(mapper::toDTO).toList();
                return ResponseEntity.ok(arr);
            }
        }

        // Page/size (ou defaults) -> retorna Page<DTO>
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 50 : Math.min(size, 500);
        var pr = PageRequest.of(p, s, Sort.by(Sort.Direction.ASC, "nome"));

        Page<SecretariaDTO> pageDTO;
        if (q != null && !q.isBlank()) {
            pageDTO = repo.findByNomeContainingIgnoreCase(q.trim(), pr).map(mapper::toDTO);
        } else {
            pageDTO = repo.findAll(pr).map(mapper::toDTO);
        }
        return ResponseEntity.ok(pageDTO);
    }

    @Operation(summary = "Cria Secretaria")
    @ApiResponse(responseCode = "201", description = "Criado",
            content = @Content(schema = @Schema(implementation = SecretariaDTO.class)))
    @PostMapping
    public ResponseEntity<SecretariaDTO> criar(@RequestBody SecretariaDTO dto){
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        var entity = mapper.toEntity(dto);
        entity.setId(null); // garante criação
        var saved = repo.save(entity);
        var body = mapper.toDTO(saved);
        return ResponseEntity.created(URI.create("/api/v1/secretarias/" + body.getId())).body(body);
    }

    @Operation(summary = "Atualiza Secretaria")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SecretariaDTO.class)))
    @PutMapping("/{id}")
    public ResponseEntity<SecretariaDTO> atualizar(@PathVariable Long id, @RequestBody SecretariaDTO dto){
        var entity = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Secretaria não encontrada"));
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        entity.setNome(dto.getNome().trim());
        var saved = repo.save(entity);
        return ResponseEntity.ok(mapper.toDTO(saved));
    }

    @Operation(summary = "Exclui Secretaria")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id){
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("Secretaria não encontrada");
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // =============  NOVO ENDPOINT UNIFICADO (mesmo controller)  ==========
    // =====================================================================

    /**
     * GET /secretarias/{id}/responsaveis
     * Dois modos:
     * - Autocomplete: ?q=...&limit=10           -> retorna ARRAY de ResponsavelDTO
     * - Lista paginada: ?q=...&page=0&size=10   -> retorna Page<ResponsavelDTO>
     */
    @Operation(
            summary = "Lista Responsáveis por Secretaria",
            description = "Retorna responsáveis vinculados à Secretaria (por ID). Suporta filtro por nome (q). " +
                    "Use 'limit' para array (autocomplete) OU 'page/size' para paginação.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Secretaria não encontrada")
            }
    )
    @GetMapping("/{id}/responsaveis")
    public ResponseEntity<?> listarResponsaveisPorSecretaria(
            @Parameter(description = "ID da Secretaria") @PathVariable("id") Long secretariaId,
            @Parameter(description = "Filtro por nome (contém)") @RequestParam(value = "q", required = false) String q,
            @Parameter(description = "Página (0..N)") @RequestParam(value = "page", required = false) Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(value = "size", required = false) Integer size,
            @Parameter(description = "Limite para autocomplete (retorna array)") @RequestParam(value = "limit", required = false) Integer limit
    ) {
        // valida secretaria
        if (!repo.existsById(secretariaId)) {
            return ResponseEntity.notFound().build();
        }

        boolean wantsArray = (limit != null) && (page == null && size == null);

        if (wantsArray) {
            int lim = Math.max(1, Math.min(limit, 500));
            var pr = PageRequest.of(0, lim, Sort.by(Sort.Direction.ASC, "nome"));
            if (q != null && !q.isBlank()) {
                var result = responsavelRepo
                        .findByNomeContainingIgnoreCaseAndSecretaria_Id(q.trim(), secretariaId, pr);
                List<ResponsavelDTO> arr = result.stream().map(responsavelMapper::toDTO).toList();
                return ResponseEntity.ok(arr);
            } else {
                var result = responsavelRepo
                        .findBySecretaria_Id(secretariaId, pr);
                List<ResponsavelDTO> arr = result.stream().map(responsavelMapper::toDTO).toList();
                return ResponseEntity.ok(arr);
            }
        }

        // modo paginado
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 10 : Math.min(size, 500);
        var pr = PageRequest.of(p, s, Sort.by(Sort.Direction.ASC, "nome"));

        Page<ResponsavelDTO> pageDTO;
        if (q != null && !q.isBlank()) {
            pageDTO = responsavelRepo
                    .findByNomeContainingIgnoreCaseAndSecretaria_Id(q.trim(), secretariaId, pr)
                    .map(responsavelMapper::toDTO);
        } else {
            pageDTO = responsavelRepo
                    .findBySecretaria_Id(secretariaId, pr)
                    .map(responsavelMapper::toDTO);
        }

        return ResponseEntity.ok(pageDTO);
    }
}
