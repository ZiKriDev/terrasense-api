package br.com.devlovers.domain.password.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDTO(

    @NotBlank
    @Email
    String email
) {

}
