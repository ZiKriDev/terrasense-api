package br.com.devlovers.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

    @NotBlank
    @Email
    String login,
    
    @NotBlank
    String password
) {

}
