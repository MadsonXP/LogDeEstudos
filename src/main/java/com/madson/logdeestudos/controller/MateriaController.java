package com.madson.logdeestudos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.madson.logdeestudos.model.Materia;
import com.madson.logdeestudos.repository.MateriaRepository;

@RestController
@RequestMapping("/materias")
public class MateriaController {

    private final MateriaRepository repository;

    public MateriaController(MateriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Materia> listar() {
        return repository.findAll();
    }
}