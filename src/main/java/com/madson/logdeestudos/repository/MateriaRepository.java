package com.madson.logdeestudos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madson.logdeestudos.model.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
    // Isso aqui já nos dá métodos prontos como: save(), findAll(), findById()
    // Sem precisar escrever SQL!
}