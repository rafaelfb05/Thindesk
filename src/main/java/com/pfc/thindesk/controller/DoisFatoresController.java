package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.CodigoDTO;
import com.pfc.thindesk.security.VerificacaoPendente;
import com.pfc.thindesk.service.DoisFatoresService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DoisFatoresController {

    @Autowired
    private DoisFatoresService doisFatoresService;
    @Autowired
    private UserDetailsService userDetailsService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @GetMapping("/verificar-codigo")
    public String formulario(HttpSession session, Model model) {
        VerificacaoPendente verificacao = doisFatoresService.buscarVerificacao( session);
        if (verificacao == null) {
            return "redirect:/";
        }
        model.addAttribute("codigoForm", new CodigoDTO());
        preencherTela(model, verificacao);
        return "verificar-codigo";
    }

    @PostMapping("/verificar-codigo")
    public String verificar(@Valid @ModelAttribute("codigoForm") CodigoDTO dto,
                            BindingResult result,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            Model model) {
        HttpSession session = request.getSession(false);
        VerificacaoPendente verificacao = (session == null) ? null : doisFatoresService.buscarVerificacao(session);
        if (verificacao == null) {
            return "redirect:/login";
        }

        if (!result.hasErrors()) {
            switch (doisFatoresService.verificar(session, dto.getCodigo())) {
                case OK -> {
                    return concluirLogin(verificacao.getEmail(), request, response)
                            ? "redirect:/" : "redirect:/login?error";
                }
                case INVALIDO -> result.rejectValue("codigo", "codigo.invalido", "Código incorreto");
                case EXPIRADO -> result.rejectValue("codigo", "codigo.expirado",
                        "Código expirado. Peça um novo código.");
                case TENTATIVAS_ESGOTADAS -> {
                    return "redirect:/login?codigoBloqueado";
                }
            }
        }
        preencherTela(model, verificacao);
        return "verificar-codigo";
    }

    @PostMapping("/verificar-codigo/reenviar")
    public String reenviar(HttpSession session) {
        if (doisFatoresService.buscarVerificacao(session) == null) {
            return "redirect:/login";
        }
        return doisFatoresService.reenviar(session)
                ? "redirect:/verificar-codigo?reenviado"
                : "redirect:/verificar-codigo?aguarde";
    }

    private boolean concluirLogin(String email, HttpServletRequest request, HttpServletResponse response) {
        UserDetails usuario = userDetailsService.loadUserByUsername(email);
        if (!usuario.isEnabled()) {
            return false;
        }
        Authentication autenticacao = UsernamePasswordAuthenticationToken
                .authenticated(usuario, null, usuario.getAuthorities());

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
        return true;
    }

    private void preencherTela(Model model, VerificacaoPendente verificacao) {
        model.addAttribute("emailMascarado", doisFatoresService.mascararEmail(verificacao.getEmail()));
        model.addAttribute("envioFalhou", verificacao.isEnvioFalhou());
    }
}
