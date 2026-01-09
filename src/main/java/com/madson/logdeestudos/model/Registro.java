package com.madson.logdeestudos.model;

import java.time.LocalDate;
import java.time.LocalTime; // <--- ÚNICA IMPORTAÇÃO NOVA NECESSÁRIA

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;
    private Integer totalQuestoes; // Nota Máxima (se for Redação)
    private Integer acertos;       // Nota Obtida (se for Redação)
    
    // Campo de texto livre para o usuário digitar o tema
    private String tema; 

    // --- NOVO CAMPO: TEMPO DE ESTUDO ---
    private LocalTime tempo; 
    // -----------------------------------

    @ManyToOne
    @JoinColumn(name = "assunto_id")
    private Assunto assunto;

    public Registro() {
        this.data = LocalDate.now();
    }

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Integer getTotalQuestoes() { return totalQuestoes; }
    public void setTotalQuestoes(Integer totalQuestoes) { this.totalQuestoes = totalQuestoes; }

    public Integer getAcertos() { return acertos; }
    public void setAcertos(Integer acertos) { this.acertos = acertos; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    // --- NOVOS GETTER E SETTER PARA O TEMPO ---
    public LocalTime getTempo() { return tempo; }
    public void setTempo(LocalTime tempo) { this.tempo = tempo; }
    // ------------------------------------------

    public Assunto getAssunto() { return assunto; }
    public void setAssunto(Assunto assunto) { this.assunto = assunto; }

    // --- SEUS MÉTODOS ORIGINAIS (MANTIDOS) ---

    public Double getAproveitamento() {
        if (totalQuestoes == null || totalQuestoes == 0) return 0.0;
        return (double) acertos / totalQuestoes * 100;
    }

    public String getStatus() {
        double porc = getAproveitamento();
        if (porc >= 80) return "EXCELENTE 🤩";
        if (porc >= 70) return "BOM 🙂";
        if (porc >= 50) return "REGULAR 😐";
        return "PRECISA MELHORAR 🙁";
    }
}