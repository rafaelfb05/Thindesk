package com.pfc.thindesk.config;

import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.enums.TipoUsuario;
import com.pfc.thindesk.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.nome}")
    private String adminNome;
    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.senha}")
    private String adminSenha;

    @Override
    public void run(String... args) {
        String email = adminEmail.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .nome(adminNome)
                .email(email)
                .senha(passwordEncoder.encode(adminSenha))
                .tipo(TipoUsuario.ADMIN)
                .build());
        log.info("Usuário ADMIN inicial criado: {}", email);
    }
}
