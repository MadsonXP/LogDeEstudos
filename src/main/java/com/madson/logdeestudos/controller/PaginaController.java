package com.madson.logdeestudos.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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

    // --- CONFIGURAÇÃO DA META (Pode mudar aqui se quiser aumentar a dificuldade) ---
    private final int META_SEMANAL = 50; 

    public PaginaController(RegistroRepository registroRepository, AssuntoRepository assuntoRepository, MateriaRepository materiaRepository) {
        this.registroRepository = registroRepository;
        this.assuntoRepository = assuntoRepository;
        this.materiaRepository = materiaRepository;
    }

    @GetMapping("/")
    public String carregarDashboard(@RequestParam(defaultValue = "7") Integer dias, 
                                    @RequestParam(required = false) Long materiaId, 
                                    Model model) {
        List<Registro> todos = registroRepository.findAll();

        // 1. CÁLCULO DA META SEMANAL (Gamificação)
        // Pega tudo dos últimos 7 dias (independente de filtro de matéria)
        LocalDate seteDiasAtras = LocalDate.now().minusDays(6); // Hoje + 6 dias atrás
        int questoesUltimaSemana = todos.stream()
            .filter(r -> !r.getData().isBefore(seteDiasAtras)) // Filtra data
            .filter(r -> r.getTema() == null || r.getTema().isEmpty()) // Só conta Questões (não Redação)
            .mapToInt(Registro::getTotalQuestoes)
            .sum();

        int porcentagemMeta = (int) ((double) questoesUltimaSemana / META_SEMANAL * 100);
        if (porcentagemMeta > 100) porcentagemMeta = 100; // Trava em 100% visualmente

        model.addAttribute("metaAlvo", META_SEMANAL);
        model.addAttribute("metaAtual", questoesUltimaSemana);
        model.addAttribute("metaPorcentagem", porcentagemMeta);

        // --- RESTANTE DO CÓDIGO (Igual ao anterior) ---
        List<Registro> soQuestoes = todos.stream()
            .filter(r -> r.getTema() == null || r.getTema().isEmpty())
            .filter(r -> materiaId == null || r.getAssunto().getMateria().getId().equals(materiaId))
            .collect(Collectors.toList());

        List<Registro> soRedacoes = todos.stream()
            .filter(r -> r.getTema() != null && !r.getTema().isEmpty())
            .collect(Collectors.toList());

        int totalQuest = soQuestoes.stream().mapToInt(Registro::getTotalQuestoes).sum();
        int totalAcertos = soQuestoes.stream().mapToInt(Registro::getAcertos).sum();
        double aproveitamento = totalQuest > 0 ? (double) totalAcertos / totalQuest * 100 : 0;
        long totalRedacoesContagem = soRedacoes.size();

        model.addAttribute("kpiTotal", totalQuest);
        model.addAttribute("kpiAcertos", totalAcertos);
        model.addAttribute("kpiPorcentagem", String.format("%.1f", aproveitamento));
        model.addAttribute("kpiRedacoes", totalRedacoesContagem);
        
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("materiaSelecionada", materiaId);

        LocalDate dataFim = LocalDate.now();
        LocalDate ultimaQuestao = soQuestoes.stream().map(Registro::getData).max(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate ultimaRedacao = soRedacoes.stream().map(Registro::getData).max(LocalDate::compareTo).orElse(LocalDate.now());
        if (ultimaQuestao.isAfter(dataFim)) dataFim = ultimaQuestao;
        if (ultimaRedacao.isAfter(dataFim)) dataFim = ultimaRedacao;

        List<String> labels = new ArrayList<>();
        List<Integer> dadosTotal = new ArrayList<>();
        List<Integer> dadosAcertos = new ArrayList<>();
        List<Integer> dadosErros = new ArrayList<>();
        List<Integer> dadosRedacaoNotas = new ArrayList<>();

        boolean isModoAnual = (dias > 31); 

        if (isModoAnual) { 
            LocalDate cursor = dataFim.minusDays(dias).withDayOfMonth(1); 
            DateTimeFormatter fmtMes = DateTimeFormatter.ofPattern("MMM", new Locale("pt", "BR"));

            while (!cursor.isAfter(dataFim)) {
                YearMonth mesAtual = YearMonth.from(cursor);
                
                List<Registro> qMes = soQuestoes.stream().filter(r -> YearMonth.from(r.getData()).equals(mesAtual)).collect(Collectors.toList());
                int t = qMes.stream().mapToInt(Registro::getTotalQuestoes).sum();
                int a = qMes.stream().mapToInt(Registro::getAcertos).sum();
                
                List<Registro> rMes = soRedacoes.stream().filter(r -> YearMonth.from(r.getData()).equals(mesAtual)).collect(Collectors.toList());
                double mediaRedacao = rMes.stream().mapToInt(Registro::getAcertos).average().orElse(0);

                labels.add(cursor.format(fmtMes).toUpperCase());
                dadosTotal.add(t);
                dadosAcertos.add(a);
                dadosErros.add(t - a);
                dadosRedacaoNotas.add((int) mediaRedacao);

                cursor = cursor.plusMonths(1);
            }
        } else { 
            LocalDate cursor = dataFim.minusDays(dias - 1); 
            DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("dd/MM");

            while (!cursor.isAfter(dataFim)) {
                LocalDate diaAtual = cursor;
                List<Registro> qDia = soQuestoes.stream().filter(r -> r.getData().equals(diaAtual)).collect(Collectors.toList());
                int t = qDia.stream().mapToInt(Registro::getTotalQuestoes).sum();
                int a = qDia.stream().mapToInt(Registro::getAcertos).sum();

                List<Registro> rDia = soRedacoes.stream().filter(r -> r.getData().equals(diaAtual)).collect(Collectors.toList());
                double mediaRedacao = rDia.stream().mapToInt(Registro::getAcertos).average().orElse(0);

                labels.add(cursor.format(fmtDia));
                dadosTotal.add(t);
                dadosAcertos.add(a);
                dadosErros.add(t - a);
                dadosRedacaoNotas.add((int) mediaRedacao);

                cursor = cursor.plusDays(1);
            }
        }

        model.addAttribute("graficoLabels", labels);
        model.addAttribute("graficoTotal", dadosTotal);
        model.addAttribute("graficoAcertos", dadosAcertos);
        model.addAttribute("graficoErros", dadosErros);
        model.addAttribute("graficoRedacaoNotas", dadosRedacaoNotas);
        model.addAttribute("periodoSelecionado", dias);

        return "dashboard";
    }

    // --- MÉTODOS DE ROTINA (MANTIDOS IGUAIS) ---
    @GetMapping("/historico")
    public String carregarHistorico(Model model) {
        List<Registro> lista = registroRepository.findAll().stream().sorted(Comparator.comparing(Registro::getId).reversed()).collect(Collectors.toList());
        model.addAttribute("listaRegistros", lista);
        return "historico";
    }

    @GetMapping("/novo")
    public String carregarFormulario(Model model) {
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("registro", new Registro());
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
    public String salvarRegistro(@RequestParam(required = false) Long id, @RequestParam LocalDate data, @RequestParam Long assuntoId, @RequestParam Integer total, @RequestParam Integer acertos, @RequestParam(required = false) String tema) {
        Registro registro = (id != null) ? registroRepository.findById(id).orElseThrow() : new Registro();
        registro.setData(data);
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