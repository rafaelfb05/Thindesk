package com.pfc.thindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodigoDTO {
    @NotBlank(message = "Digite o código")
    @Pattern(regexp = "^\\s*\\d{6}\\s*$", message = "O código tem 6 números")
    private String codigo;
}
