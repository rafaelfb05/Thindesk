package com.pfc.thindesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsqueciSenhaDTO {

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    private String email;
}
