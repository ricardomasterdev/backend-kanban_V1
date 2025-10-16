// src/main/java/com/seuprojeto/kanban/dto/SecretariaDTO.java
package com.seuprojeto.kanban.dto;

public class SecretariaDTO {
    private Long id;
    private String nome;

    public SecretariaDTO() {}

    public SecretariaDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
