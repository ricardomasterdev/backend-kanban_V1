package com.seuprojeto.kanban.mapper;
import com.seuprojeto.kanban.domain.*; import com.seuprojeto.kanban.dto.*; import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface SecretariaMapper { SecretariaDTO toDTO(Secretaria s); @Mapping(target="id", ignore=true) Secretaria toEntity(SecretariaDTO dto); }
