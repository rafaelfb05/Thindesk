package com.pfc.thindesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pfc.thindesk.entity.Cliente;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.entity.HorarioAtendimento;
import com.pfc.thindesk.service.ClienteService;
import com.pfc.thindesk.service.ChamadoService;
import com.pfc.thindesk.service.HorarioAtendimentoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private ChamadoService chamadoService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private HorarioAtendimentoService horarioAtendimentoService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        boolean equipe = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TECNICO"));

        List<Chamado> lista = equipe
                ? chamadoService.listarChamados()
                : chamadoService.listarPorUsuario(auth.getName());

        long total = lista.size();
        long abertos = contar(lista, "ABERTO");
        long andamento = contar(lista, "EM_ANDAMENTO");
        long resolvidos = contar(lista, "CONCLUIDO", "RESOLVIDO");
        long cancelados = contar(lista, "CANCELADO");

        model.addAttribute("escopo", equipe ? "Todos os chamados do sistema" : "Seus chamados");
        model.addAttribute("total", total);
        model.addAttribute("abertos", abertos);
        model.addAttribute("andamento", andamento);
        model.addAttribute("resolvidos", resolvidos);
        model.addAttribute("cancelados", cancelados);
        model.addAttribute("pctAbertos", porcentagem(abertos, total));
        model.addAttribute("pctAndamento", porcentagem(andamento, total));
        model.addAttribute("pctResolvidos", porcentagem(resolvidos, total));
        model.addAttribute("pctCancelados", porcentagem(cancelados, total));
        return "home";
    }

    private static long contar(List<Chamado> lista, String... statuses) {
        return lista.stream()
                .filter(c -> c.getStatus() != null)
                .filter(c -> {
                    for (String s : statuses) {
                        if (c.getStatus().equalsIgnoreCase(s)) return true;
                    }
                    return false;
                })
                .count();
    }

    private static long porcentagem(long parte, long total) {
        return total == 0 ? 0 : Math.round(parte * 100.0 / total);
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }

    @GetMapping("/chamados")
    public String chamados(Model model, Authentication auth) {
        boolean equipe = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TECNICO"));

        List<Chamado> chamados = equipe
                ? chamadoService.listarChamados()
                : chamadoService.listarPorUsuario(auth.getName());

        model.addAttribute("chamados", chamados);
        model.addAttribute("content", "chamados :: content");
        return "chamados";
    }

    @GetMapping("/clientes")
    public String clientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("content", "clientes :: content");
        return "clientes";
    }
    
    @GetMapping("/ajustes-horarios")
    public String horarios(Model model) {
        List<HorarioAtendimento> horarios = horarioAtendimentoService.listarTodos();
        model.addAttribute("horarios", horarios);
        model.addAttribute("novoHorario", new HorarioAtendimento());
        model.addAttribute("content", "ajustes-horarios :: content");
        return "ajustes-horarios";
    }

}
