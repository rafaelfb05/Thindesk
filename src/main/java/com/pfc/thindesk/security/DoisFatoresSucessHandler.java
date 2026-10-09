package com.pfc.thindesk.security;

import com.pfc.thindesk.service.DoisFatoresService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DoisFatoresSucessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private DoisFatoresService doisFatoresService;

    private final AuthenticationSuccessHandler comportamentoPadrao =
            new SimpleUrlAuthenticationSuccessHandler("/");

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        if (!doisFatoresService.isAtivo()) {
            comportamentoPadrao.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession();
        session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        doisFatoresService.iniciarVerificacao(session, authentication.getName());
        response.sendRedirect(request.getContextPath() + "/verificar-codigo");
    }
}
