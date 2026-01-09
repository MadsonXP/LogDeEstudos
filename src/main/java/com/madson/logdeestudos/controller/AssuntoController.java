package com.madson.logdeestudos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // <--- Importante para o novo método
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.madson.logdeestudos.model.Assunto;
import com.madson.logdeestudos.model.Materia;
import com.madson.logdeestudos.repository.AssuntoRepository;
import com.madson.logdeestudos.repository.MateriaRepository;

@RestController
@RequestMapping("/assuntos")
public class AssuntoController {

    private final AssuntoRepository assuntoRepository;
    private final MateriaRepository materiaRepository;

    // O Spring injeta os repositórios automaticamente aqui
    public AssuntoController(AssuntoRepository assuntoRepository, MateriaRepository materiaRepository) {
        this.assuntoRepository = assuntoRepository;
        this.materiaRepository = materiaRepository;
    }

    // 1. Listar todos os assuntos (Geral)
    @GetMapping
    public List<Assunto> listarTodos() {
        return assuntoRepository.findAll();
    }

    // 2. [NOVO] Buscar assuntos de uma matéria específica
    // Exemplo de uso: /assuntos/por-materia/2 (Traz só os assuntos de Matemática)
    @GetMapping("/por-materia/{materiaId}")
    public List<Assunto> buscarPorMateria(@PathVariable Long materiaId) {
        return assuntoRepository.findByMateriaId(materiaId);
    }

    // 3. Criar um novo assunto
    // JSON esperado: { "nome": "Logaritmos", "materiaId": 2 }
    @PostMapping
    public Assunto criar(@RequestBody NovoAssuntoRequest request) {
        // Busca a matéria pelo ID (ex: ID 2 = Matemática)
        Materia materia = materiaRepository.findById(request.materiaId)
                .orElseThrow(() -> new RuntimeException("Matéria não encontrada!"));

        // Cria o assunto e liga com a matéria
        Assunto novoAssunto = new Assunto();
        novoAssunto.setNome(request.nome);
        novoAssunto.setMateria(materia);

        return assuntoRepository.save(novoAssunto);
    }

    // Classe auxiliar apenas para receber os dados do JSON
    public static class NovoAssuntoRequest {
        public String nome;
        public Long materiaId;
    }
    
}