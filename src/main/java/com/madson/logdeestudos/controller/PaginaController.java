package com.madson.logdeestudos.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public PaginaController(RegistroRepository registroRepository, AssuntoRepository assuntoRepository, MateriaRepository materiaRepository) {
        this.registroRepository = registroRepository;
        this.assuntoRepository = assuntoRepository;
        this.materiaRepository = materiaRepository;
    }

    // --- DASHBOARD ---
    @GetMapping("/")
    public String carregarDashboard(@RequestParam(defaultValue = "7") Integer dias, Model model) {
        List<Registro> todos = registroRepository.findAll();
        
        List<Registro> soQuestoes = todos.stream()
            .filter(r -> r.getTema() == null || r.getTema().isEmpty())
            .collect(Collectors.toList());

        int totalQuest = soQuestoes.stream().mapToInt(Registro::getTotalQuestoes).sum();
        int totalAcertos = soQuestoes.stream().mapToInt(Registro::getAcertos).sum();
        double aproveitamento = totalQuest > 0 ? (double) totalAcertos / totalQuest * 100 : 0;
        long totalRedacoes = todos.stream().filter(r -> r.getTema() != null && !r.getTema().isEmpty()).count();

        LocalDate dataLimite = LocalDate.now().minusDays(dias);
        
        Map<LocalDate, List<Registro>> agrupadoPorData = soQuestoes.stream()
                .filter(r -> !r.getData().isBefore(dataLimite))
                .collect(Collectors.groupingBy(Registro::getData, TreeMap::new, Collectors.toList()));

        List<String> labels = new ArrayList<>();
        List<Integer> dadosTotal = new ArrayList<>();
        List<Integer> dadosAcertos = new ArrayList<>();
        List<Integer> dadosErros = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        for (Map.Entry<LocalDate, List<Registro>> entrada : agrupadoPorData.entrySet()) {
            labels.add(entrada.getKey().format(fmt));
            int t = entrada.getValue().stream().mapToInt(Registro::getTotalQuestoes).sum();
            int a = entrada.getValue().stream().mapToInt(Registro::getAcertos).sum();
            dadosTotal.add(t);
            dadosAcertos.add(a);
            dadosErros.add(t - a);
        }

        model.addAttribute("kpiTotal", totalQuest);
        model.addAttribute("kpiAcertos", totalAcertos);
        model.addAttribute("kpiPorcentagem", String.format("%.1f", aproveitamento));
        model.addAttribute("kpiRedacoes", totalRedacoes);
        model.addAttribute("graficoLabels", labels);
        model.addAttribute("graficoTotal", dadosTotal);
        model.addAttribute("graficoAcertos", dadosAcertos);
        model.addAttribute("graficoErros", dadosErros);
        model.addAttribute("periodoSelecionado", dias);

        return "dashboard";
    }

    // --- HISTÓRICO ---
    @GetMapping("/historico")
    public String carregarHistorico(Model model) {
        List<Registro> lista = registroRepository.findAll().stream()
                .sorted(Comparator.comparing(Registro::getId).reversed()) // Mais recentes no topo
                .collect(Collectors.toList());
        model.addAttribute("listaRegistros", lista);
        return "historico";
    }

    // --- CADASTRO ---
    @GetMapping("/novo")
    public String carregarFormulario(Model model) {
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("registro", new Registro()); // Data já vem como "hoje" por padrão
        return "registrar";
    }

    @GetMapping("/editar/{id}")
    public String carregarEdicao(@PathVariable Long id, Model model) {
        Registro antigo = registroRepository.findById(id).orElseThrow();
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("registro", antigo);
        return "registrar";
    }

    @PostMapping("/salvar")
    public String salvarRegistro(@RequestParam(required = false) Long id, 
                                 @RequestParam LocalDate data, // <--- NOVO: Recebe a data do formulário
                                 @RequestParam Long assuntoId, 
                                 @RequestParam Integer total, 
                                 @RequestParam Integer acertos,
                                 @RequestParam(required = false) String tema) {
        
        Registro registro = (id != null) ? registroRepository.findById(id).orElseThrow() : new Registro();
        
        registro.setData(data); // <--- Atualiza com a data escolhida
        registro.setAssunto(assuntoRepository.findById(assuntoId).orElseThrow());
        registro.setTotalQuestoes(total);
        registro.setAcertos(acertos);
        registro.setTema(tema);
        
        registroRepository.save(registro);
        
        return "redirect:/historico";
    }

    @GetMapping("/deletar/{id}")
    public String deletarRegistro(@PathVariable Long id) {
        registroRepository.deleteById(id);
        return "redirect:/historico";
    }
}