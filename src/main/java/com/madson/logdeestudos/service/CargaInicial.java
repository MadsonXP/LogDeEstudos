package com.madson.logdeestudos.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.madson.logdeestudos.model.Materia;
import com.madson.logdeestudos.repository.MateriaRepository;

@Component
public class CargaInicial implements CommandLineRunner {

    private final MateriaRepository repository;

    public CargaInicial(MateriaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já tem matérias cadastradas. Se não tiver, cadastra as do Ensino Médio.
        if (repository.count() == 0) {
            
            List<Materia> materias = Arrays.asList(
                new Materia("Português"),
                new Materia("Matemática"),
                new Materia("História"),
                new Materia("Geografia"),
                new Materia("Física"),
                new Materia("Química"),
                new Materia("Biologia"),
                new Materia("Inglês"),
                new Materia("Filosofia"),
                new Materia("Sociologia"),
                new Materia("Artes"),
                new Materia("Literatura"),
                new Materia("Redação")
            );

            repository.saveAll(materias);
            System.out.println("--- Matérias do Ensino Médio cadastradas com sucesso! ---");
        }
    }
}