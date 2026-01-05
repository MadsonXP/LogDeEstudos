package com.madson.logdeestudos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Assunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome; // Ex: Regra de Três, Revolução Francesa

    @ManyToOne // Vários assuntos -> Uma matéria
    @JoinColumn(name = "materia_id") // Cria uma coluna extra para guardar o ID da matéria
    private Materia materia;

    // Construtor vazio (obrigatório pro Java)
    public Assunto() {
    }

    // Construtor para facilitar a criação
    public Assunto(String nome, Materia materia) {
        this.nome = nome;
        this.materia = materia;
    }

    // Getters e Setters manuais
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }
}