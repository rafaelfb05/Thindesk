package com.pfc.thindesk.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "chamados")
@Getter
@Setter
public class Chamado{
    
    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String status;
    private String tipo;
    private String tecnico;
    private String usuario;

    private LocalDateTime dataAbertura;
    private LocalDateTime dataResolucao;
    private String solucao;

}
