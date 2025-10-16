package com.seuprojeto.kanban.web;

import com.seuprojeto.kanban.dto.ResponsavelCreateUpdateDTO;
import com.seuprojeto.kanban.dto.ResponsavelDTO;
import com.seuprojeto.kanban.mapper.ResponsavelMapper;
import com.seuprojeto.kanban.repository.ResponsavelRepository;
import com.seuprojeto.kanban.repository.SecretariaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/responsaveis")
@Tag(name = "Responsáveis", description = "Cadastro e manutenção de responsáveis (e-mail único) e vínculo com Secretarias.")
@SecurityRequirement(name = "bearerAuth")
public class ResponsavelController {

    private final ResponsavelRepository repo;
    private final SecretariaRepository secretariaRepo;
    private final ResponsavelMapper mapper;

    public ResponsavelController(ResponsavelRepository repo,
                                 SecretariaRepository secretariaRepo,
                                 ResponsavelMapper mapper) {
        this.repo = repo;
        this.secretariaRepo = secretariaRepo;
        this.mapper = mapper;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ResponsavelDTO> criar(@Validated @RequestBody ResponsavelCreateUpdateDTO dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        var e = mapper.toEntity(dto);
        if (dto.secretariaId() != null) {
            var s = secretariaRepo.findById(dto.secretariaId()).orElseThrow();
            e.setSecretaria(s);
        }
        e = repo.save(e);
        return ResponseEntity.ok(mapper.toDTO(e));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponsavelDTO> atualizar(
            @Parameter(description = "ID do responsável") @PathVariable Long id,
            @Validated @RequestBody ResponsavelCreateUpdateDTO dto) {

        var e = repo.findById(id).orElseThrow();

        if (!e.getEmail().equalsIgnoreCase(dto.email()) && repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        e.setNome(dto.nome());
        e.setEmail(dto.email());
        e.setCargo(dto.cargo());

        if (dto.secretariaId() != null) {
            var s = secretariaRepo.findById(dto.secretariaId()).orElseThrow();
            e.setSecretaria(s);
        } else {
            e.setSecretaria(null);
        }

        e = repo.save(e);
        return ResponseEntity.ok(mapper.toDTO(e));
    }

    // Lista com filtros q (nome) e secretariaId — ambos paginados
    @Operation(summary = "Lista responsáveis", description = "Retorna lista paginada com filtros por nome (q) e secretariaId.")
    @GetMapping
    public ResponseEntity<Page<ResponsavelDTO>> listar(
            @Parameter(description = "Página (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filtro por nome (contém)") @RequestParam(required = false) String q,
            @Parameter(description = "Filtro por secretariaId") @RequestParam(required = false) Long secretariaId
    ) {
        var pageable = PageRequest.of(page, size);
        Page<com.seuprojeto.kanban.domain.Responsavel> result;

        if (q != null && !q.isBlank() && secretariaId != null) {
            result = repo.findByNomeContainingIgnoreCaseAndSecretaria_Id(q.trim(), secretariaId, pageable);
        } else if (q != null && !q.isBlank()) {
            result = repo.findByNomeContainingIgnoreCase(q.trim(), pageable);
        } else if (secretariaId != null) {
            result = repo.findBySecretaria_Id(secretariaId, pageable);
        } else {
            result = repo.findAll(pageable);
        }

        return ResponseEntity.ok(result.map(mapper::toDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsavelDTO> obter(@PathVariable Long id) {
        var e = repo.findById(id).orElseThrow();
        return ResponseEntity.ok(mapper.toDTO(e));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
