package br.com.devlovers.domain.register.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(

    @NotBlank
    String token,

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password
) {

}
