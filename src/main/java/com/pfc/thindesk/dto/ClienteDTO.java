package com.pfc.thindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteDTO {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Nome muito longo")
    private String nome;

    @NotBlank(message = "Informe o telefone")
    @Size(max = 20, message = "Telefone muito longo")
    private String telefone;

    @NotBlank(message = "Informe o setor")
    @Size(max = 50, message = "Setor muito longo")
    private String setor;
}
