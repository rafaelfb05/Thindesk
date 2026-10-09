package com.pfc.thindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedefinirSenhaDTO {

    @NotBlank
    private String token;

    @NotBlank(message = "Informe a nova senha")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "A senha deve ter de 8 a 64 caracteres, com maiúscula, minúscula, número e símbolo"
    )
    private String senha;

    @NotBlank(message = "Confirme a senha")
    private String confirmarSenha;
}
