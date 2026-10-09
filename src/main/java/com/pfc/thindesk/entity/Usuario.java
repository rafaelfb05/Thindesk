package com.pfc.thindesk.entity;

import com.pfc.thindesk.enums.TipoUsuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {

    @Id
    private String id;
    private String nome;
    @Indexed(unique = true)
    private String email;
    private String senha;
    private TipoUsuario tipo;
    private String clienteId;
    @Builder.Default
    private boolean ativo = true;
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

}
