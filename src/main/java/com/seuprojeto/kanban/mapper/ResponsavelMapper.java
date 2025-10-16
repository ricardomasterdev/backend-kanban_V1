package com.seuprojeto.kanban.mapper;

import com.seuprojeto.kanban.domain.Responsavel;
import com.seuprojeto.kanban.dto.ResponsavelCreateUpdateDTO;
import com.seuprojeto.kanban.dto.ResponsavelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResponsavelMapper {

    // ENTITY -> DTO
    @Mapping(target = "secretariaId",
            expression = "java(e.getSecretaria() != null ? e.getSecretaria().getId() : null)")
    ResponsavelDTO toDTO(Responsavel e);

    // DTO -> ENTITY (a associação Secretaria será setada no controller/service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secretaria", ignore = true)
    Responsavel toEntity(ResponsavelCreateUpdateDTO dto);
}
