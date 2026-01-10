package com.madson.logdeestudos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madson.logdeestudos.model.Registro;
import com.madson.logdeestudos.model.Usuario;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    
    // Busca todos os registros que pertencem a um usuário específico
    List<Registro> findByUsuario(Usuario usuario);
    
}