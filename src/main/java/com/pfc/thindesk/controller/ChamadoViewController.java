package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.service.ChamadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chamados")
public class ChamadoViewController {

    @Autowired
    private ChamadoService chamadoService;

    @GetMapping("/novo")
    public String formNovo(Model model) {
        model.addAttribute("chamado", new Chamado());
        return "chamado-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Chamado chamado, Authentication auth) {
        chamado.setId(null);

        if (isEquipe(auth)) {
            if (chamado.getStatus() == null || chamado.getStatus().isBlank()) {
                chamado.setStatus("ABERTO");
            }
        } else {
            chamado.setUsuario(auth.getName());
            chamado.setStatus("ABERTO");
            chamado.setTecnico(null);
        }
        chamadoService.criarChamado(chamado);
        return "redirect:/chamados";
    }

    @GetMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public String formAtualizar(@PathVariable String id, Model model) {
        model.addAttribute("chamado", chamadoService.buscarPorId(id));
        return "atualizar";
    }

    @PostMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public String atualizar(@PathVariable String id, @ModelAttribute Chamado chamado) {
        chamadoService.atualizarChamado(id, chamado);
        return "redirect:/chamados";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable String id, Authentication auth) {
        Chamado chamado = chamadoService.buscarPorId(id);
        if (!isEquipe(auth) && !auth.getName().equals(chamado.getUsuario())) {
            throw new AccessDeniedException("Você só pode cancelar os seus chamados");
        }
        chamadoService.cancelarChamado(id);
        return "redirect:/chamados";
    }

    private boolean isEquipe(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TECNICO"));
    }
}
