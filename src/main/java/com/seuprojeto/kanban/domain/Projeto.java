package com.seuprojeto.kanban.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(
        name = "projeto",
        indexes = {
                @Index(name = "idx_projeto_status", columnList = "status"),
                @Index(name = "idx_projeto_secretaria_id", columnList = "secretaria_id")
        }
)
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusProjeto status = StatusProjeto.A_INICIAR;

    @Column(name = "inicio_previsto")
    private LocalDate inicioPrevisto;

    @Column(name = "termino_previsto")
    private LocalDate terminoPrevisto;

    @Column(name = "inicio_realizado")
    private LocalDate inicioRealizado;

    @Column(name = "termino_realizado")
    private LocalDate terminoRealizado;

    @Column(name = "dias_atraso", nullable = false)
    private int diasAtraso = 0;

    @Column(name = "percentual_tempo_restante", nullable = false)
    private int percentualTempoRestante = 0;

    // ======= VÍNCULO N:N Projeto × Responsável =======
    @ManyToMany
    @JoinTable(
            name = "projeto_responsavel",
            joinColumns = @JoinColumn(name = "projeto_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "responsavel_id", referencedColumnName = "id")
    )
    private Set<Responsavel> responsaveis = new HashSet<>();

    // ======= VÍNCULO N:1 Projeto × Secretaria =======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaria_id")
    private Secretaria secretaria;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // getters/setters
    public UUID getId(){ return id; }
    public void setId(UUID id){ this.id = id; }

    public String getNome(){ return nome; }
    public void setNome(String nome){ this.nome = nome; }

    public StatusProjeto getStatus(){ return status; }
    public void setStatus(StatusProjeto s){ this.status = s; }

    public LocalDate getInicioPrevisto(){ return inicioPrevisto; }
    public void setInicioPrevisto(LocalDate v){ this.inicioPrevisto = v; }

    public LocalDate getTerminoPrevisto(){ return terminoPrevisto; }
    public void setTerminoPrevisto(LocalDate v){ this.terminoPrevisto = v; }

    public LocalDate getInicioRealizado(){ return inicioRealizado; }
    public void setInicioRealizado(LocalDate v){ this.inicioRealizado = v; }

    public LocalDate getTerminoRealizado(){ return terminoRealizado; }
    public void setTerminoRealizado(LocalDate v){ this.terminoRealizado = v; }

    public int getDiasAtraso(){ return diasAtraso; }
    public void setDiasAtraso(int v){ this.diasAtraso = v; }

    public int getPercentualTempoRestante(){ return percentualTempoRestante; }
    public void setPercentualTempoRestante(int v){ this.percentualTempoRestante = v; }

    public Set<Responsavel> getResponsaveis(){ return responsaveis; }
    public void setResponsaveis(Set<Responsavel> r){ this.responsaveis = r; }

    public Secretaria getSecretaria(){ return secretaria; }
    public void setSecretaria(Secretaria s){ this.secretaria = s; }

    public Instant getCreatedAt(){ return createdAt; }
    public Instant getUpdatedAt(){ return updatedAt; }
}
