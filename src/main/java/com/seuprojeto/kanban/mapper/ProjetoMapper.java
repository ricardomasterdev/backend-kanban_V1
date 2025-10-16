package com.seuprojeto.kanban.mapper;

import com.seuprojeto.kanban.domain.Projeto;
import com.seuprojeto.kanban.domain.Responsavel;
import com.seuprojeto.kanban.domain.Secretaria;
import com.seuprojeto.kanban.dto.ProjetoCreateUpdateDTO;
import com.seuprojeto.kanban.dto.ProjetoDTO;
import com.seuprojeto.kanban.dto.ResponsavelShortDTO;
import com.seuprojeto.kanban.dto.SecretariaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProjetoMapper {

    // ENTITY -> DTO
    @Mapping(target = "responsaveisIds", expression = "java(mapResponsaveisIds(p))")
    @Mapping(target = "secretariaId", expression = "java(p.getSecretaria() != null ? p.getSecretaria().getId() : null)")
    @Mapping(target = "secretaria", expression = "java(toSecretariaDTO(p.getSecretaria()))")
    @Mapping(target = "responsaveis", expression = "java(toResponsavelShortList(p))")
    public abstract ProjetoDTO toDTO(Projeto p);

    // DTO -> ENTITY (vínculos resolvidos no service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "diasAtraso", ignore = true)
    @Mapping(target = "percentualTempoRestante", ignore = true)
    @Mapping(target = "responsaveis", ignore = true)
    @Mapping(target = "secretaria", ignore = true)
    public abstract Projeto toEntity(ProjetoCreateUpdateDTO dto);

    // ===== helpers =====
    protected Set<Long> mapResponsaveisIds(Projeto p) {
        if (p.getResponsaveis() == null || p.getResponsaveis().isEmpty()) return Set.of();
        return p.getResponsaveis().stream().map(Responsavel::getId).collect(Collectors.toSet());
    }

    protected SecretariaDTO toSecretariaDTO(Secretaria s) {
        if (s == null) return null;
        return new SecretariaDTO(s.getId(), s.getNome());
    }

    protected List<ResponsavelShortDTO> toResponsavelShortList(Projeto p) {
        if (p.getResponsaveis() == null || p.getResponsaveis().isEmpty()) return List.of();
        return p.getResponsaveis().stream()
                .map(r -> new ResponsavelShortDTO(r.getId(), r.getNome()))
                .collect(Collectors.toList());
    }
}
