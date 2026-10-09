package com.pfc.thindesk.enums;

public enum TipoUsuario {
    ADMIN("Administrador"),
    TECNICO("Técnico"),
    CLIENTE("Cliente");

    private final String descricao;

    TipoUsuario(String descricao){
        this.descricao=descricao;
    }

    public String getDescricao(){
        return descricao;
    }
}
