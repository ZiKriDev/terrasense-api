package br.com.devlovers.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterAuthDTO(
    
    @NotBlank
    @Email
    String login,

    @NotBlank
    String password
    
    ) {

}
