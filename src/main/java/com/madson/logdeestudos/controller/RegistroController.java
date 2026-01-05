package com.madson.logdeestudos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.madson.logdeestudos.model.Assunto;
import com.madson.logdeestudos.model.Registro;
import com.madson.logdeestudos.repository.AssuntoRepository;
import com.madson.logdeestudos.repository.RegistroRepository;

@RestController
@RequestMapping("/registros")
public class RegistroController {

    private final RegistroRepository registroRepository;
    private final AssuntoRepository assuntoRepository;

    public RegistroController(RegistroRepository registroRepository, AssuntoRepository assuntoRepository) {
        this.registroRepository = registroRepository;
        this.assuntoRepository = assuntoRepository;
    }

    @GetMapping
    public List<Registro> listar() {
        return registroRepository.findAll();
    }

    @PostMapping
    public Registro criar(@RequestBody NovoRegistroRequest request) {
        Assunto assunto = assuntoRepository.findById(request.assuntoId)
                .orElseThrow(() -> new RuntimeException("Assunto não encontrado!"));

        Registro novo = new Registro();
        novo.setTotalQuestoes(request.totalQuestoes);
        novo.setAcertos(request.acertos);
        novo.setAssunto(assunto);
        // A data já é preenchida automaticamente com hoje no construtor

        return registroRepository.save(novo);
    }

    // Classe auxiliar para receber os dados
    public static class NovoRegistroRequest {
        public Long assuntoId;
        public Integer totalQuestoes;
        public Integer acertos;
    }
}