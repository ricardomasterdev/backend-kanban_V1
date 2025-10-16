// src/main/java/com/seuprojeto/kanban/spec/ProjetoSpecs.java
package com.seuprojeto.kanban.spec;

import com.seuprojeto.kanban.domain.Projeto;
import com.seuprojeto.kanban.domain.StatusProjeto;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;

public final class ProjetoSpecs {
    private ProjetoSpecs() {}

    public static Specification<Projeto> nomeLike(String texto) {
        if (texto == null || texto.isBlank()) return null;
        var like = "%" + texto.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("nome")), like);
    }

    public static Specification<Projeto> statusEq(StatusProjeto status) {
        if (status == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Projeto> secretariaIdEq(Long secretariaId) {
        if (secretariaId == null) return null;
        return (root, cq, cb) ->
                cb.equal(root.join("secretaria", JoinType.LEFT).get("id"), secretariaId);
    }
}
