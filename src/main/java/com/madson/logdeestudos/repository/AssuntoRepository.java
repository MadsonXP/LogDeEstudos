package com.madson.logdeestudos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madson.logdeestudos.model.Assunto;

public interface AssuntoRepository extends JpaRepository<Assunto, Long> {
    // Método extra para buscar assuntos pelo ID da matéria
    // Ex: Trazer todos os assuntos de Matemática (ID 2)
    List<Assunto> findByMateriaId(Long materiaId);
}