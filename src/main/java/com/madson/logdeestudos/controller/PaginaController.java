package com.madson.logdeestudos.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.madson.logdeestudos.model.Registro;
import com.madson.logdeestudos.model.Usuario;
import com.madson.logdeestudos.repository.AssuntoRepository;
import com.madson.logdeestudos.repository.MateriaRepository;
import com.madson.logdeestudos.repository.RegistroRepository;
import com.madson.logdeestudos.repository.UsuarioRepository;
import com.madson.logdeestudos.service.UsuarioService;

@Controller
public class PaginaController {

    private final RegistroRepository registroRepository;
    private final AssuntoRepository assuntoRepository;
    private final MateriaRepository materiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public PaginaController(RegistroRepository registroRepository, AssuntoRepository assuntoRepository, MateriaRepository materiaRepository, UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.registroRepository = registroRepository;
        this.assuntoRepository = assuntoRepository;
        this.materiaRepository = materiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    private Usuario getUsuarioLogado(Principal principal) {
        if (principal == null) return null;
        String email = principal.getName();
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/login")
    public String carregarLogin() {
        return "login";
    }

    @GetMapping("/registrar")
    public String carregarCadastroUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro_usuario";
    }

    @PostMapping("/salvar-usuario")
    public String salvarUsuario(@ModelAttribute Usuario usuario, HttpServletRequest request, Model model) {
        try {
            String siteURL = request.getRequestURL().toString().replace(request.getServletPath(), "");
            usuarioService.registrar(usuario, siteURL);
            return "registro_sucesso";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
            return "registro_usuario";
        }
    }

    @GetMapping("/verificar")
    public String verificarConta(@RequestParam("codigo") String codigo) {
        boolean verificado = usuarioService.verificar(codigo);
        return verificado ? "verificacao_sucesso" : "login?error";
    }

    // --- ROTAS DE PERFIL ---
    @GetMapping("/perfil")
    public String carregarPerfil(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    // 👇 O MÉTODO ATUALIZADO COM SENHA ATUAL 👇
    @PostMapping("/atualizar-perfil")
    public String atualizarPerfil(@RequestParam String nome, 
                                  @RequestParam(required = false) String senhaAtual,
                                  @RequestParam(required = false) String novaSenha,
                                  Principal principal,
                                  Model model) {
        Usuario usuario = getUsuarioLogado(principal);
        
        try {
            usuarioService.atualizarPerfil(usuario, nome, senhaAtual, novaSenha);
            model.addAttribute("sucesso", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            model.addAttribute("erro", "Erro: " + e.getMessage());
        }
        
        model.addAttribute("usuario", usuario);
        return "perfil";
    }
    // ----------------------------------------

    @GetMapping("/cronometro")
    public String carregarCronometro(Model model) {
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        return "cronometro";
    }

    @PostMapping("/salvar")
    public String salvarRegistro(@RequestParam(required = false) Long id, 
                                 @RequestParam LocalDate data, 
                                 @RequestParam Long assuntoId, 
                                 @RequestParam(defaultValue = "0") Integer total, 
                                 @RequestParam(defaultValue = "0") Integer acertos, 
                                 @RequestParam(required = false) String tema,
                                 @RequestParam(required = false) LocalTime tempo,
                                 Principal principal) { 
        
        Registro registro = (id != null) ? registroRepository.findById(id).orElseThrow() : new Registro();
        
        if (id != null) {
            Usuario dono = registro.getUsuario();
            if (dono != null && !dono.getEmail().equals(principal.getName())) {
                return "redirect:/historico"; 
            }
        }

        registro.setData(data);
        registro.setAssunto(assuntoRepository.findById(assuntoId).orElseThrow());
        registro.setTotalQuestoes(total);
        registro.setAcertos(acertos);
        registro.setTema(tema);
        registro.setTempo(tempo);
        registro.setUsuario(getUsuarioLogado(principal));
        
        registroRepository.save(registro);
        return "redirect:/historico";
    }

    @GetMapping("/")
    public String carregarDashboard(@RequestParam(defaultValue = "7") Integer dias, 
                                    @RequestParam(required = false) Long materiaId, 
                                    Model model,
                                    Principal principal) {
        
        Usuario usuario = getUsuarioLogado(principal);
        if (usuario != null) {
            model.addAttribute("nomeUsuario", usuario.getNome());
        }

        List<Registro> todos = registroRepository.findByUsuario(usuario);

        int streak = 0;
        List<LocalDate> diasEstudados = todos.stream().map(Registro::getData).distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        if (!diasEstudados.isEmpty()) {
            LocalDate hoje = LocalDate.now();
            LocalDate ontem = hoje.minusDays(1);
            LocalDate ultimoEstudo = diasEstudados.get(0);
            if (ultimoEstudo.equals(hoje) || ultimoEstudo.equals(ontem)) {
                streak = 1;
                LocalDate dataCheck = ultimoEstudo;
                for (int i = 1; i < diasEstudados.size(); i++) {
                    if (diasEstudados.get(i).equals(dataCheck.minusDays(1))) { streak++; dataCheck = diasEstudados.get(i); } else { break; }
                }
            }
        }
        model.addAttribute("foguinho", streak);

        int META_SEMANAL = 50;
        LocalDate seteDiasAtras = LocalDate.now().minusDays(6);
        int questoesUltimaSemana = todos.stream().filter(r -> !r.getData().isBefore(seteDiasAtras)).filter(r -> r.getTema() == null || r.getTema().isEmpty()).mapToInt(Registro::getTotalQuestoes).sum();
        int porcentagemMeta = (int) ((double) questoesUltimaSemana / META_SEMANAL * 100);
        if (porcentagemMeta > 100) porcentagemMeta = 100;
        model.addAttribute("metaAlvo", META_SEMANAL);
        model.addAttribute("metaAtual", questoesUltimaSemana);
        model.addAttribute("metaPorcentagem", porcentagemMeta);

        List<Registro> registrosFiltrados = todos.stream().filter(r -> materiaId == null || r.getAssunto().getMateria().getId().equals(materiaId)).collect(Collectors.toList());
        List<Registro> soQuestoes = registrosFiltrados.stream().filter(r -> r.getTema() == null || r.getTema().isEmpty()).collect(Collectors.toList());
        List<Registro> soRedacoes = registrosFiltrados.stream().filter(r -> r.getTema() != null && !r.getTema().isEmpty()).collect(Collectors.toList());

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
        LocalDate ultimaData = registrosFiltrados.stream().map(Registro::getData).max(LocalDate::compareTo).orElse(LocalDate.now());
        if (ultimaData.isAfter(dataFim)) dataFim = ultimaData;

        List<String> labels = new ArrayList<>();
        List<Integer> dadosTotal = new ArrayList<>();
        List<Integer> dadosAcertos = new ArrayList<>();
        List<Integer> dadosErros = new ArrayList<>();
        List<Integer> dadosRedacaoNotas = new ArrayList<>();
        List<Double> dadosHorasEstudadas = new ArrayList<>();

        boolean isModoAnual = (dias > 31); 
        
        if (isModoAnual) { 
            LocalDate cursor = dataFim.minusDays(dias).withDayOfMonth(1);
            DateTimeFormatter fmtMes = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("pt-BR"));
            while (!cursor.isAfter(dataFim)) {
                YearMonth mesAtual = YearMonth.from(cursor);
                List<Registro> rMes = registrosFiltrados.stream().filter(r -> YearMonth.from(r.getData()).equals(mesAtual)).collect(Collectors.toList());
                List<Registro> qMes = rMes.stream().filter(r -> r.getTema() == null || r.getTema().isEmpty()).collect(Collectors.toList());
                List<Registro> redMes = rMes.stream().filter(r -> r.getTema() != null && !r.getTema().isEmpty()).collect(Collectors.toList());

                int t = qMes.stream().mapToInt(Registro::getTotalQuestoes).sum();
                int a = qMes.stream().mapToInt(Registro::getAcertos).sum();
                double mediaRedacao = redMes.stream().mapToInt(Registro::getAcertos).average().orElse(0);
                double segundosMes = rMes.stream().filter(r -> r.getTempo() != null).mapToDouble(r -> r.getTempo().toSecondOfDay()).sum();
                
                labels.add(cursor.format(fmtMes).toUpperCase());
                dadosTotal.add(t); dadosAcertos.add(a); dadosErros.add(t - a);
                dadosRedacaoNotas.add((int) mediaRedacao);
                dadosHorasEstudadas.add(segundosMes / 3600.0);
                cursor = cursor.plusMonths(1);
            }
        } else { 
            LocalDate cursor = dataFim.minusDays(dias - 1);
            DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("dd/MM");
            while (!cursor.isAfter(dataFim)) {
                LocalDate diaAtual = cursor;
                List<Registro> rDia = registrosFiltrados.stream().filter(r -> r.getData().equals(diaAtual)).collect(Collectors.toList());
                List<Registro> qDia = rDia.stream().filter(r -> r.getTema() == null || r.getTema().isEmpty()).collect(Collectors.toList());
                List<Registro> redDia = rDia.stream().filter(r -> r.getTema() != null && !r.getTema().isEmpty()).collect(Collectors.toList());

                int t = qDia.stream().mapToInt(Registro::getTotalQuestoes).sum();
                int a = qDia.stream().mapToInt(Registro::getAcertos).sum();
                double mediaRedacao = redDia.stream().mapToInt(Registro::getAcertos).average().orElse(0);
                double segundosDia = rDia.stream().filter(r -> r.getTempo() != null).mapToDouble(r -> r.getTempo().toSecondOfDay()).sum();

                labels.add(cursor.format(fmtDia));
                dadosTotal.add(t); dadosAcertos.add(a); dadosErros.add(t - a);
                dadosRedacaoNotas.add((int) mediaRedacao);
                dadosHorasEstudadas.add(segundosDia / 3600.0);
                cursor = cursor.plusDays(1);
            }
        }

        LocalDate dataCorte = isModoAnual ? dataFim.minusDays(dias).withDayOfMonth(1) : dataFim.minusDays(dias - 1);
        List<Registro> registrosDoPeriodo = registrosFiltrados.stream().filter(r -> !r.getData().isBefore(dataCorte)).filter(r -> r.getTempo() != null).collect(Collectors.toList());
        Map<String, Double> mapaMateriaTempo = new HashMap<>();
        for (Registro r : registrosDoPeriodo) {
            String nomeMat = r.getAssunto().getMateria().getNome();
            double segundos = r.getTempo().toSecondOfDay();
            mapaMateriaTempo.put(nomeMat, mapaMateriaTempo.getOrDefault(nomeMat, 0.0) + segundos);
        }
        List<String> pizzaLabels = new ArrayList<>();
        List<Double> pizzaDados = new ArrayList<>();
        mapaMateriaTempo.forEach((materia, segundos) -> { pizzaLabels.add(materia); pizzaDados.add(segundos / 3600.0); });

        model.addAttribute("graficoLabels", labels);
        model.addAttribute("graficoTotal", dadosTotal);
        model.addAttribute("graficoAcertos", dadosAcertos);
        model.addAttribute("graficoErros", dadosErros);
        model.addAttribute("graficoRedacaoNotas", dadosRedacaoNotas);
        model.addAttribute("graficoHorasEstudadas", dadosHorasEstudadas);
        model.addAttribute("pizzaLabels", pizzaLabels);
        model.addAttribute("pizzaDados", pizzaDados);
        model.addAttribute("periodoSelecionado", dias);
        return "dashboard";
    }

    @GetMapping("/historico")
    public String carregarHistorico(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        if (usuario != null) {
            model.addAttribute("nomeUsuario", usuario.getNome());
        }

        List<Registro> todos = registroRepository.findByUsuario(usuario).stream()
                .sorted(Comparator.comparing(Registro::getId).reversed())
                .collect(Collectors.toList());
        
        List<Registro> listaQuestoes = todos.stream()
                .filter(r -> (r.getTotalQuestoes() != null && r.getTotalQuestoes() > 0) || (r.getTema() != null && !r.getTema().isEmpty()))
                .collect(Collectors.toList());

        List<Registro> listaTempo = todos.stream()
                .filter(r -> r.getTempo() != null)
                .collect(Collectors.toList());

        model.addAttribute("listaQuestoes", listaQuestoes);
        model.addAttribute("listaTempo", listaTempo);
        
        return "historico";
    }

    @GetMapping("/novo")
    public String carregarFormulario(Model model) {
        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("registro", new Registro());
        return "registrar";
    }

    @GetMapping("/editar/{id}")
    public String carregarEdicao(@PathVariable Long id, Model model, Principal principal) {
        Registro antigo = registroRepository.findById(id).orElseThrow();
        
        Usuario dono = antigo.getUsuario();
        if (dono != null && !dono.getEmail().equals(principal.getName())) {
            return "redirect:/historico"; 
        }

        model.addAttribute("listaMaterias", materiaRepository.findAll());
        model.addAttribute("registro", antigo);
        return "registrar";
    }

    @GetMapping("/deletar/{id}")
    public String deletarRegistro(@PathVariable Long id, Principal principal) {
        Registro registro = registroRepository.findById(id).orElseThrow();
        
        Usuario dono = registro.getUsuario();
        if (dono != null && dono.getEmail().equals(principal.getName())) {
            registroRepository.deleteById(id);
        }
        
        return "redirect:/historico";
    }
}