package com.pfc.thindesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastroDTO {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Nome muito longo")
    private String nome;

    @NotBlank(message = "Informe o telefone")
    @Size(max = 20, message = "Telefone muito longo")
    private String telefone;

    @NotBlank(message = "Informe o setor")
    @Size(max = 50, message = "Setor muito longo")
    private String setor;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Informe a senha")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "A senha deve ter de 8 a 64 caracteres, com maiúscula, minúscula, número e símbolo"
    )
    private String senha;

    @NotBlank(message = "Confirme a senha")
    private String confirmarSenha;
}
