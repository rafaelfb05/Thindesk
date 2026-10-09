package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.EsqueciSenhaDTO;
import com.pfc.thindesk.dto.RedefinirSenhaDTO;
import com.pfc.thindesk.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecuperacaoSenhaController {

    @Autowired
    private RecuperacaoSenhaService recuperacaoService;

    @GetMapping("/esqueci-senha")
    public String formEsqueci(Model model) {
        model.addAttribute("esqueci", new EsqueciSenhaDTO());
        return "esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String solicitar(@Valid @ModelAttribute("esqueci") EsqueciSenhaDTO dto,
                            BindingResult result) {
        if (result.hasErrors()) {
            return "esqueci-senha";
        }
        recuperacaoService.solicitar(dto.getEmail());
        return "redirect:/esqueci-senha?enviado";
    }

    @GetMapping("/redefinir-senha")
    public String formRedefinir(@RequestParam(required = false) String token, Model model) {
        if (!recuperacaoService.tokenValido(token)) {
            model.addAttribute("tokenInvalido", true);
        }
        RedefinirSenhaDTO dto = new RedefinirSenhaDTO();
        dto.setToken(token);
        model.addAttribute("redefinir", dto);
        return "redefinir-senha";
    }

    @PostMapping("/redefinir-senha")
    public String redefinir(@Valid @ModelAttribute("redefinir") RedefinirSenhaDTO dto,
                            BindingResult result,
                            Model model) {
        if (dto.getSenha() != null && !dto.getSenha().equals(dto.getConfirmarSenha())) {
            result.rejectValue("confirmarSenha", "senha.diferente", "As senhas não conferem");
        }
        if (result.hasErrors()) {
            return "redefinir-senha";
        }
        try {
            recuperacaoService.redefinir(dto.getToken(), dto.getSenha());
        } catch (IllegalArgumentException e) {
            model.addAttribute("tokenInvalido", true);
            return "redefinir-senha";
        }
        return "redirect:/login?senhaAlterada";
    }
}
