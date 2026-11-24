package com.pfc.thindesk.config;

import com.pfc.thindesk.security.DoisFatoresSucessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DoisFatoresSucessHandler doisFatores) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/registro", "/esqueci-senha", "/redefinir-senha", "/verificar-codigo/**",
                                "/acesso-negado", "/error",  "/css/**", "/dist/**", "/plugins/**", "/js/**", "/images/**")
                        .permitAll()

                        .requestMatchers("/admin/**", "/tecnicos/**", "/ajustes-horarios/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/clientes/**")
                        .hasAnyRole("ADMIN", "TECNICO")

                        .requestMatchers("/chamados/**")
                        .hasAnyRole("ADMIN", "TECNICO", "CLIENTE")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .successHandler(doisFatores)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/acesso-negado")
                );
        return http.build();
    }
}
