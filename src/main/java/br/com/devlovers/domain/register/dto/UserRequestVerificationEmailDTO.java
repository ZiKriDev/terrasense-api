package br.com.devlovers.domain.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestVerificationEmailDTO(

    @NotBlank
    @Email
    String email
) {

}
