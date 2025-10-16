package com.seuprojeto.kanban.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(
        name = "secretaria",
        indexes = {
                @Index(name = "idx_secretaria_nome", columnList = "nome", unique = true)
        }
)
public class Secretaria implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150, unique = true)
    private String nome;

    public Secretaria() { }

    public Secretaria(String nome) {
        this.nome = nome;
    }

    // Normaliza o campo antes de persistir/atualizar
    @PrePersist
    @PreUpdate
    private void prePersistUpdate() {
        if (this.nome != null) {
            // trim + colapsa espaços múltiplos
            this.nome = this.nome.trim().replaceAll("\\s+", " ");
        }
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    // equals/hashCode por id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Secretaria)) return false;
        Secretaria that = (Secretaria) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Secretaria{id=" + id + ", nome='" + nome + "'}";
    }
}
