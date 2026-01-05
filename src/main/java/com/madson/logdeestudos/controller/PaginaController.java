package com.madson.logdeestudos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.madson.logdeestudos.model.Registro;
import com.madson.logdeestudos.repository.AssuntoRepository;
import com.madson.logdeestudos.repository.MateriaRepository;
import com.madson.logdeestudos.repository.RegistroRepository;

@Controller
public class PaginaController {

    private final RegistroRepository registroRepository;
    private final AssuntoRepository assuntoRepository;
    private final MateriaRepository materiaRepository;

    public PaginaController(RegistroRepository registroRepository, 
                            AssuntoRepository assuntoRepository,
                            MateriaRepository materiaRepository) {
        this.registroRepository = registroRepository;
        this.assuntoRepository = assuntoRepository;
        this.materiaRepository = materiaRepository;
    }

    @GetMapping("/")
    public String carregarHome(Model model) {
        model.addAttribute("listaRegistros", registroRepository.findAll());
        return "home";
    }

    // TELA DE NOVO (Cria um registro vazio para o formulário não dar erro)
    @GetMapping("/novo")
    public String carregarFormulario(Model model) {
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("registro", new Registro()); // Envia objeto vazio
        return "registrar";
    }

    // TELA DE EDITAR (Busca o registro antigo e manda para o formulário)
    @GetMapping("/editar/{id}")
    public String carregarEdicao(@PathVariable Long id, Model model) {
        Registro antigo = registroRepository.findById(id).orElseThrow();
        
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("registro", antigo); // Envia o registro cheio
        return "registrar";
    }

    @PostMapping("/salvar")
    public String salvarRegistro(@RequestParam(required = false) Long id, // ID agora é opcional
                                 @RequestParam Long assuntoId, 
                                 @RequestParam Integer total, 
                                 @RequestParam Integer acertos,
                                 @RequestParam(required = false) String tema) {
        
        Registro registro;

        if (id != null) {
            // Se tem ID, estamos EDITANDO um existente
            registro = registroRepository.findById(id).orElseThrow();
        } else {
            // Se não tem ID, estamos CRIANDO um novo
            registro = new Registro();
        }

        var assunto = assuntoRepository.findById(assuntoId).orElseThrow();
        
        registro.setAssunto(assunto);
        registro.setTotalQuestoes(total);
        registro.setAcertos(acertos);
        registro.setTema(tema);
        
        registroRepository.save(registro); // O 'save' serve para criar e atualizar

        return "redirect:/";
    }

    @GetMapping("/deletar/{id}")
    public String deletarRegistro(@PathVariable Long id) {
        registroRepository.deleteById(id);
        return "redirect:/";
    }
}