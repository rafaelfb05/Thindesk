package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.enums.StatusChamado;
import com.pfc.thindesk.exception.RegraNegocioException;
import com.pfc.thindesk.service.ChamadoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chamados")
public class ChamadoViewController {

    private final ChamadoService chamadoService;

    public ChamadoViewController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @GetMapping("/novo")
    public String formNovo(Model model) {
        model.addAttribute("chamado", new Chamado());
        return "chamado-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Chamado chamado, Authentication auth, RedirectAttributes redirect) {
        if (vazio(chamado.getTitulo()) || vazio(chamado.getDescricao())) {
            redirect.addFlashAttribute("erro", "Título e descrição são obrigatórios");
            return "redirect:/chamados/novo";
        }

        chamado.setId(null);
        chamado.setTitulo(chamado.getTitulo().trim());
        chamado.setDescricao(chamado.getDescricao().trim());

        if (isEquipe(auth)) {
            if (chamado.getStatus() == null || chamado.getStatus().isBlank()) {
                chamado.setStatus(StatusChamado.ABERTO.name());
            }
        } else {
            chamado.setUsuario(auth.getName());
            chamado.setStatus(StatusChamado.ABERTO.name());
            chamado.setTecnico(null);
        }
        try {
            chamadoService.criarChamado(chamado);
            redirect.addFlashAttribute("sucesso", "Chamado aberto com sucesso");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chamados";
    }

    @GetMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public String formAtualizar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        Chamado chamado = chamadoService.buscarPorId(id);
        if (StatusChamado.RESOLVIDO.name().equals(chamado.getStatus())) {
            redirect.addFlashAttribute("erro", "Chamado resolvido não pode ser alterado");
            return "redirect:/chamados";
        }
        model.addAttribute("chamado", chamado);
        return "chamados/atualizar";
    }

    @PostMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public String atualizar(@PathVariable String id, @ModelAttribute Chamado chamado,
                            RedirectAttributes redirect) {
        try {
            chamadoService.atualizarChamado(id, chamado);
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chamados";
    }

    @GetMapping("/resolver/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public String formResolver(@PathVariable String id, Model model, RedirectAttributes redirect) {
        Chamado chamado = chamadoService.buscarPorId(id);
        if (StatusChamado.RESOLVIDO.name().equals(chamado.getStatus())) {
            redirect.addFlashAttribute("erro", "Chamado resolvido não pode ser alterado");
            return "redirect:/chamados";
        }
        model.addAttribute("chamado", chamado);
        return "chamado-resolver";
    }

    @PostMapping("/resolver/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public String resolver(@PathVariable String id, @RequestParam String solucao,
                           Authentication auth, RedirectAttributes redirect) {
        if (vazio(solucao)) {
            redirect.addFlashAttribute("erro", "A solução é obrigatória");
            return "redirect:/chamados/resolver/" + id;
        }
        Chamado chamado = chamadoService.buscarPorId(id);
        if (StatusChamado.RESOLVIDO.name().equals(chamado.getStatus())) {
            redirect.addFlashAttribute("erro", "Chamado resolvido não pode ser alterado");
            return "redirect:/chamados";
        }
        try {
            chamadoService.resolverChamado(id, solucao, auth.getName());
            redirect.addFlashAttribute("sucesso", "Chamado resolvido com sucesso");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chamados";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable String id, Authentication auth, RedirectAttributes redirect) {
        Chamado chamado = chamadoService.buscarPorId(id);
        if (!isEquipe(auth) && !auth.getName().equals(chamado.getUsuario())) {
            throw new AccessDeniedException("Você só pode cancelar os seus chamados");
        }
        try {
            chamadoService.cancelarChamado(id);
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chamados";
    }

    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean isEquipe(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TECNICO"));
    }
}