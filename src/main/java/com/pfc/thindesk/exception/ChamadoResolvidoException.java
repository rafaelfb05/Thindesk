package com.pfc.thindesk.exception;

public class ChamadoResolvidoException extends RegraNegocioException{

    public ChamadoResolvidoException(){
        super("Chamado já resolvido não pode ser alterado");
    }
}
