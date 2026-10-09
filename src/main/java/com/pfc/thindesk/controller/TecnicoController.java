package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.TecnicoDTO;
import com.pfc.thindesk.enums.TipoUsuario;
import com.pfc.thindesk.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tecnicos")
@PreAuthorize("hasRole('ADMIN')")
public class TecnicoController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", usuarioService.listarPorTipo(TipoUsuario.TECNICO));
        return "tecnicos";
    }

    @GetMapping("/novo")
    public String formNovo(Model model) {
        model.addAttribute("tecnico", new TecnicoDTO());
        return "tecnico-form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("tecnico") TecnicoDTO dto,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "tecnico-form";
        }
        try {
            usuarioService.cadastrarTecnico(dto);
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "email.duplicado", e.getMessage());
            return "tecnico-form";
        }
        redirect.addFlashAttribute("sucesso", "Técnico cadastrado");
        return "redirect:/tecnicos";
    }
}
