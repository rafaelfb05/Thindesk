package com.pfc.thindesk.controller;

import com.pfc.thindesk.enums.TipoUsuario;
import com.pfc.thindesk.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("tipos", TipoUsuario.values());
        return "admin/usuarios";
    }

    @PostMapping("/{id}/tipo")
    public String alterarTipo(@PathVariable String id,
                              @RequestParam TipoUsuario tipo,
                              Authentication auth,
                              RedirectAttributes redirect) {
        try {
            usuarioService.alterarTipo(id, tipo, auth.getName());
            redirect.addFlashAttribute("sucesso", "Perfil atualizado");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/ativo")
    public String alterarAtivo(@PathVariable String id,
                               @RequestParam boolean ativo,
                               Authentication auth,
                               RedirectAttributes redirect) {
        try {
            usuarioService.alterarAtivo(id, ativo, auth.getName());
            redirect.addFlashAttribute("sucesso", ativo ? "Conta ativada" : "Conta desativada");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
