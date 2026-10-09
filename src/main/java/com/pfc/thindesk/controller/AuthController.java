package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.CadastroDTO;
import com.pfc.thindesk.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("cadastro", new CadastroDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("cadastro") CadastroDTO dto,
                            BindingResult result) {

        if (dto.getSenha() != null && !dto.getSenha().equals(dto.getConfirmarSenha())) {
            result.rejectValue("confirmarSenha", "senha.diferente", "As senhas não conferem");
        }

        if (result.hasErrors()) {
            return "registro";
        }

        try {
            usuarioService.cadastrarCliente(dto);
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "email.duplicado", e.getMessage());
            return "registro";
        }

        return "redirect:/login?registrado";
    }
}
