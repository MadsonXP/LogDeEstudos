package com.madson.logdeestudos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    public AssuntoController(AssuntoRepository assuntoRepository, MateriaRepository materiaRepository) {
        this.assuntoRepository = assuntoRepository;
        this.materiaRepository = materiaRepository;
    }

    @GetMapping
    public List<Assunto> listarTodos() {
        return assuntoRepository.findAll();
    }

    @GetMapping("/por-materia/{materiaId}")
    public List<Assunto> buscarPorMateria(@PathVariable Long materiaId) {
        return assuntoRepository.findByMateriaId(materiaId);
    }

    @PostMapping
    public Assunto criar(@RequestBody NovoAssuntoRequest request) {
        // CORREÇÃO: Verifica se o ID é nulo antes de usar
        if (request.materiaId == null) {
            throw new IllegalArgumentException("O ID da matéria é obrigatório.");
        }

        Materia materia = materiaRepository.findById(request.materiaId)
                .orElseThrow(() -> new RuntimeException("Matéria não encontrada!"));

        Assunto novoAssunto = new Assunto();
        novoAssunto.setNome(request.nome);
        novoAssunto.setMateria(materia);

        return assuntoRepository.save(novoAssunto);
    }

    public static class NovoAssuntoRequest {
        public String nome;
        public Long materiaId;
    }
}