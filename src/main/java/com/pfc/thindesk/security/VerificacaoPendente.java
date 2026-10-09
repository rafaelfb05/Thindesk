package com.pfc.thindesk.security;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
public class VerificacaoPendente implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ATRIBUTO_SESSAO = "VERIFICACAO_2FA";

    private final String email;
    private String codigoHash;
    private Instant expiraEm;
    private Instant enviadoEm;
    private int tentativas;
    private boolean envioFalhou;

    public VerificacaoPendente(String email) {
        this.email = email;
    }
}
