package com.seuprojeto.kanban.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "responsavel",
        indexes = { @Index(name = "uk_responsavel_email", columnList = "email", unique = true) })
public class Responsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 160, unique = true)
    private String email;

    @Column(length = 120)
    private String cargo;

    @ManyToOne
    @JoinColumn(name = "secretaria_id")
    private Secretaria secretaria;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public Secretaria getSecretaria() { return secretaria; }
    public void setSecretaria(Secretaria secretaria) { this.secretaria = secretaria; }
}
