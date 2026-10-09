package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.TokenRecuperacao;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.repository.TokenRecuperacaoRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import static org.codehaus.groovy.runtime.EncodingGroovyMethods.sha256;

@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TokenRecuperacaoRepository tokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.mail.from}")
    private String remetente;
    @Value("${app.recuperacao.validade-minutos}")
    private long validadeMinutos;
    @Value("${app.recuperacao.log-link}")
    private boolean logarLink;

    public void solicitar(String emailInformado) {
        String email = emailInformado.trim().toLowerCase();
        usuarioRepository.findByEmail(email)
                .filter(Usuario::isAtivo)
                .ifPresent(this::gerarEEnviar);
    }

    public boolean tokenValido(String token) {
        return buscarTokenValido(token) != null;
    }

    public void redefinir(String token, String novaSenha) {
        TokenRecuperacao tokenSalvo = buscarTokenValido(token);
        if (tokenSalvo == null) {
            throw new IllegalArgumentException("Link inválido ou expirado");
        }
        Usuario usuario = usuarioRepository.findById(tokenSalvo.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Link inválido ou expirado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        tokenRepository.deleteByUsuarioId(usuario.getId());
    }

    private void gerarEEnviar(Usuario usuario) {
        tokenRepository.deleteByUsuarioId(usuario.getId());

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        tokenRepository.save(new TokenRecuperacao(
                null, sha256(token), usuario.getId(),
                Instant.now().plus(Duration.ofMinutes(validadeMinutos))));

        String link = baseUrl + "/redefinir-senha?token=" + token;
        enviarEmail(usuario, link);
    }

    private void enviarEmail(Usuario usuario, String link) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(usuario.getEmail());
        mensagem.setSubject("Thindesk - redefinição de senha");
        mensagem.setText("Olá, " + usuario.getNome() + "!\n\n"
                + "Recebemos um pedido para redefinir a sua senha. "
                + "Acesse o link abaixo (válido por " + validadeMinutos + " minutos):\n\n"
                + link + "\n\n"
                + "Se você não fez esse pedido, ignore este e-mail: a sua senha continua a mesma.");
        try {
            mailSender.send(mensagem);
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de recuperação para {}: {}", usuario.getEmail(), e.getMessage());
        }
        if (logarLink) {
            log.warn("[DESENVOLVIMENTO] Link de recuperação de {}: {}", usuario.getEmail(), link);
        }
    }

    private TokenRecuperacao buscarTokenValido(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokenRepository.findByTokenHash(sha256(token))
                // o TTL do Mongo apaga com atraso de até ~1 min, então conferimos aqui também
                .filter(t -> t.getExpiraEm().isAfter(Instant.now()))
                .orElse(null);
    }

    private String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
