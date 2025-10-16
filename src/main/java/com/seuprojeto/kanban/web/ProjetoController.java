package com.seuprojeto.kanban.web;

import com.seuprojeto.kanban.domain.StatusProjeto;
import com.seuprojeto.kanban.dto.ApiError;
import com.seuprojeto.kanban.dto.ProjetoCreateUpdateDTO;
import com.seuprojeto.kanban.dto.ProjetoDTO;
import com.seuprojeto.kanban.mapper.ProjetoMapper;
import com.seuprojeto.kanban.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos")
@Tag(name = "Projetos", description = "CRUD de projetos (com múltiplos responsáveis e secretaria).")
@SecurityRequirement(name = "bearerAuth")
public class ProjetoController {

    private final ProjetoService service;
    private final ProjetoMapper mapper;

    public ProjetoController(ProjetoService service, ProjetoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Lista projetos", description = "Consulta paginada/filtrada.")
    @GetMapping
    public ResponseEntity<Page<ProjetoDTO>> listar(
            @RequestParam(name = "q", required = false) String q,          // <- front usa "q"
            @RequestParam(required = false) Long secretariaId,             // <- novo filtro vindo do front
            @RequestParam(required = false) StatusProjeto status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        var res = service
                .listar(q, status, secretariaId, PageRequest.of(page, size))
                .map(mapper::toDTO);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ProjetoDTO> criar(@RequestBody ProjetoCreateUpdateDTO dto){
        var p = service.criar(dto);
        return ResponseEntity.ok(mapper.toDTO(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetoDTO> atualizar(@PathVariable java.util.UUID id, @RequestBody ProjetoCreateUpdateDTO dto){
        var p = service.atualizar(id, dto);
        return ResponseEntity.ok(mapper.toDTO(p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDTO> obter(@PathVariable java.util.UUID id){
        var p = service.obter(id);
        return ResponseEntity.ok(mapper.toDTO(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable java.util.UUID id){
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
