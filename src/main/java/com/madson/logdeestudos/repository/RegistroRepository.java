package com.madson.logdeestudos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madson.logdeestudos.model.Registro;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    // Aqui poderemos criar relatórios depois (ex: buscar por data)
}