package com.pfc.thindesk.service;

import com.pfc.thindesk.security.VerificacaoPendente;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class DoisFatoresService {

    public enum Resultado { OK, INVALIDO, EXPIRADO, TENTATIVAS_ESGOTADAS }

    private static final Logger log = LoggerFactory.getLogger(DoisFatoresService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.doisfatores.ativo}")
    private boolean ativo;
    @Value("${app.doisfatores.validade-minutos}")
    private long validadeMinutos;
    @Value("${app.doisfatores.max-tentativas}")
    private int maxTentativas;
    @Value("${app.doisfatores.intervalo-reenvio-segundos}")
    private long intervaloReenvioSegundos;

    @Value("${app.doisfatores.log-codigo}")
    private boolean logarCodigo;
    @Value("${app.mail.from}")
    private String remetente;

    public boolean isAtivo() {
        return ativo;
    }

    public void iniciarVerificacao(HttpSession session, String email) {
        gerarEEnviar(session, new VerificacaoPendente(email));
    }

    public boolean reenviar(HttpSession session) {
        VerificacaoPendente verificacao = buscarVerificacao(session);
        if (verificacao == null) {
            return false;
        }
        Instant liberadoEm = verificacao.getEnviadoEm().plusSeconds(intervaloReenvioSegundos);
        if (Instant.now().isBefore(liberadoEm)) {
            return false;
        }
        gerarEEnviar(session, verificacao);
        return true;
    }

    public Resultado verificar(HttpSession session, String codigoDigitado) {
        VerificacaoPendente verificacao = buscarVerificacao(session);
        if (verificacao == null) {
            return Resultado.EXPIRADO;
        }
        if (Instant.now().isAfter(verificacao.getExpiraEm())) {
            return Resultado.EXPIRADO;
        }
        if (passwordEncoder.matches(codigoDigitado.trim(), verificacao.getCodigoHash())) {
            cancelar(session);
            return Resultado.OK;
        }
        verificacao.setTentativas(verificacao.getTentativas() + 1);
        if (verificacao.getTentativas() >= maxTentativas) {
            cancelar(session);
            return Resultado.TENTATIVAS_ESGOTADAS;
        }

        session.setAttribute(verificacao.ATRIBUTO_SESSAO, verificacao);
        return Resultado.INVALIDO;
    }

    public VerificacaoPendente buscarVerificacao(HttpSession session) {
        Object atributo = session.getAttribute(VerificacaoPendente.ATRIBUTO_SESSAO);
        return (atributo instanceof VerificacaoPendente d) ? d : null;
    }

    public void cancelar(HttpSession session) {
        session.removeAttribute(VerificacaoPendente.ATRIBUTO_SESSAO);
    }

    public String mascararEmail(String email) {
        int arroba = email.indexOf('@');
        if (arroba <= 1) {
            return "***" + email.substring(Math.max(arroba, 0));
        }
        return email.charAt(0) + "***" + email.substring(arroba);
    }

    private void gerarEEnviar(HttpSession session, VerificacaoPendente verificacao) {
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        Instant agora = Instant.now();

        verificacao.setCodigoHash(passwordEncoder.encode(codigo));
        verificacao.setEnviadoEm(agora);
        verificacao.setExpiraEm(agora.plus(Duration.ofMinutes(validadeMinutos)));
        verificacao.setTentativas(0);
        verificacao.setEnvioFalhou(!enviarEmail(verificacao.getEmail(), codigo));

        session.setAttribute(VerificacaoPendente.ATRIBUTO_SESSAO, verificacao);

        if (logarCodigo) {
            log.warn("[DESENVOLVIMENTO] Código 2FA de {}: {}", verificacao.getEmail(), codigo);
        }
    }

    private boolean enviarEmail(String email, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("Thindesk - seu código de verificação");
        mensagem.setText("Seu código de verificação do Thindesk é: " + codigo + "\n\n"
                + "Ele vale por " + validadeMinutos + " minutos e só pode ser usado uma vez.\n\n"
                + "Se você não tentou entrar no sistema, ignore este e-mail e troque a sua senha.");
        try {
            mailSender.send(mensagem);
            return true;
        } catch (MailException e) {
            log.error("Falha ao enviar o código 2FA para {}: {}", email, e.getMessage());
            return false;
        }
    }
}
